package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.YouPlayerApplication
import com.example.data.model.CommentItem
import com.example.data.model.Resource
import com.example.data.model.VideoItem
import com.example.data.remote.CuratedVideoCatalog
import com.example.data.repository.VideoRepository
import com.example.util.LogEntry
import com.example.util.NetworkMonitor
import com.example.util.NotificationHelper
import com.example.util.StripePaymentSimulator
import com.example.util.StripePaymentState
import com.example.util.YouPlayerLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    HOME, TRENDING, LIBRARY, SETTINGS, SEARCH
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as YouPlayerApplication).database
    private val repository = VideoRepository(
        favoriteDao = db.favoriteDao(),
        historyDao = db.watchHistoryDao(),
        offlineDao = db.offlineCacheDao()
    )

    private val networkMonitor = NetworkMonitor(application)
    val stripeSimulator = StripePaymentSimulator(application)

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isSupporter: StateFlow<Boolean> = stripeSimulator.isSupporter
    val stripePaymentState: StateFlow<StripePaymentState> = stripeSimulator.paymentState

    val logs: StateFlow<List<LogEntry>> = YouPlayerLogger.logsFlow

    // Navigation
    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Categories
    val categories = listOf("All", "Trending", "Music", "Gaming", "Tech", "News", "Live")
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Videos state
    private val _trendingVideosState = MutableStateFlow<Resource<List<VideoItem>>>(Resource.Loading)
    val trendingVideosState: StateFlow<Resource<List<VideoItem>>> = _trendingVideosState.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResultsState = MutableStateFlow<Resource<List<VideoItem>>>(Resource.Success(emptyList()))
    val searchResultsState: StateFlow<Resource<List<VideoItem>>> = _searchResultsState.asStateFlow()

    private val _recentSearches = MutableStateFlow(listOf("4K HDR", "Lofi Beats", "Kurzgesagt", "Mark Rober", "Science"))
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private var searchDebounceJob: Job? = null

    // Video Player state
    private val _currentVideo = MutableStateFlow<VideoItem?>(null)
    val currentVideo: StateFlow<VideoItem?> = _currentVideo.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isFullScreen = MutableStateFlow(false)
    val isFullScreen: StateFlow<Boolean> = _isFullScreen.asStateFlow()

    private val _isPlayerCollapsed = MutableStateFlow(false)
    val isPlayerCollapsed: StateFlow<Boolean> = _isPlayerCollapsed.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isContinuousPlay = MutableStateFlow(true)
    val isContinuousPlay: StateFlow<Boolean> = _isContinuousPlay.asStateFlow()

    private val _relatedVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    val relatedVideos: StateFlow<List<VideoItem>> = _relatedVideos.asStateFlow()

    private val _comments = MutableStateFlow<List<CommentItem>>(emptyList())
    val comments: StateFlow<List<CommentItem>> = _comments.asStateFlow()

    private val _isCurrentFavorite = MutableStateFlow(false)
    val isCurrentFavorite: StateFlow<Boolean> = _isCurrentFavorite.asStateFlow()

    private val _isCurrentOfflineCached = MutableStateFlow(false)
    val isCurrentOfflineCached: StateFlow<Boolean> = _isCurrentOfflineCached.asStateFlow()

    // Room Database Reactive Flows
    val favorites: StateFlow<List<VideoItem>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<VideoItem>> = repository.watchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlineVideos: StateFlow<List<VideoItem>> = repository.offlineVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadTrending(_selectedCategory.value)
        YouPlayerLogger.i("MainViewModel", "MainViewModel initialized successfully")
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
        if (tab == AppTab.TRENDING && _selectedCategory.value != "Trending") {
            selectCategory("Trending")
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        loadTrending(category)
    }

    fun loadTrending(category: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            repository.getTrending(category, forceRefresh).collect { resource ->
                _trendingVideosState.value = resource
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        searchDebounceJob?.cancel()
        if (query.isBlank()) {
            _searchResultsState.value = Resource.Success(emptyList())
            return
        }
        searchDebounceJob = viewModelScope.launch {
            delay(300) // instant 300ms debounce
            performSearch(query)
        }
    }

    fun submitSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        _searchQuery.value = trimmed
        if (!_recentSearches.value.contains(trimmed)) {
            _recentSearches.value = listOf(trimmed) + _recentSearches.value.take(7)
        }
        searchDebounceJob?.cancel()
        viewModelScope.launch {
            performSearch(trimmed)
        }
    }

    private suspend fun performSearch(query: String) {
        repository.searchVideos(query).collect { res ->
            _searchResultsState.value = res
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResultsState.value = Resource.Success(emptyList())
    }

    fun removeRecentSearch(term: String) {
        _recentSearches.value = _recentSearches.value.filter { it != term }
    }

    fun playVideo(video: VideoItem) {
        _currentVideo.value = video
        _isPlaying.value = true
        _isPlayerCollapsed.value = false

        viewModelScope.launch {
            repository.recordHistory(video)
            _relatedVideos.value = repository.getRelatedVideos(video.id)
            _comments.value = repository.getComments(video.id)

            // Check favorite and offline status
            db.favoriteDao().isFavorite(video.id).collect { isFav ->
                _isCurrentFavorite.value = isFav
            }
        }
        viewModelScope.launch {
            db.offlineCacheDao().isCached(video.id).collect { isCached ->
                _isCurrentOfflineCached.value = isCached
            }
        }
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun minimizePlayer() {
        _isPlayerCollapsed.value = true
    }

    fun expandPlayer() {
        _isPlayerCollapsed.value = false
    }

    fun closePlayer() {
        _currentVideo.value = null
        _isPlaying.value = false
        _isPlayerCollapsed.value = false
        _isFullScreen.value = false
    }

    fun toggleFullScreen() {
        _isFullScreen.value = !_isFullScreen.value
    }

    fun setFullScreen(fullScreen: Boolean) {
        _isFullScreen.value = fullScreen
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        YouPlayerLogger.d("Player", "Speed set to ${speed}x")
    }

    fun toggleContinuousPlay() {
        _isContinuousPlay.value = !_isContinuousPlay.value
        YouPlayerLogger.d("Player", "Continuous play toggled: ${_isContinuousPlay.value}")
    }

    fun playNextRelatedVideo() {
        val next = _relatedVideos.value.firstOrNull() ?: CuratedVideoCatalog.ALL_VIDEOS.random()
        playVideo(next)
    }

    fun toggleCurrentFavorite() {
        val video = _currentVideo.value ?: return
        viewModelScope.launch {
            repository.toggleFavorite(video)
        }
    }

    fun toggleVideoFavorite(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleFavorite(video)
        }
    }

    fun toggleCurrentOfflineCache() {
        val video = _currentVideo.value ?: return
        viewModelScope.launch {
            if (_isCurrentOfflineCached.value) {
                repository.removeOfflineCache(video.id)
            } else {
                repository.cacheForOffline(video)
            }
        }
    }

    fun toggleVideoOfflineCache(video: VideoItem) {
        viewModelScope.launch {
            val isCached = db.offlineCacheDao().getAllCached()
            // Toggle
            repository.toggleOfflineCache(video)
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun triggerTrendingNotification() {
        val topVideo = CuratedVideoCatalog.ALL_VIDEOS.first()
        NotificationHelper.showTrendingNotification(
            getApplication(),
            topVideo.title,
            topVideo.channelTitle
        )
    }

    fun processStripeSupporter(amount: String = "$4.99", tierName: String = "YouPlayer Supporter") {
        viewModelScope.launch {
            stripeSimulator.processStripePayment(amount, tierName)
        }
    }

    fun resetStripeState() {
        stripeSimulator.resetPaymentState()
    }

    fun clearDebugLogs() {
        YouPlayerLogger.clear()
    }
}
