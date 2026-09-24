package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DebugLogSheet
import com.example.ui.components.MiniPlayer
import com.example.ui.components.StripeCheckoutSheet
import com.example.ui.components.VideoDetailView
import com.example.ui.components.VideoPlayerView
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DarkDivider
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouPlayerApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isSupporter by viewModel.isSupporter.collectAsStateWithLifecycle()
    val stripePaymentState by viewModel.stripePaymentState.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    val currentVideo by viewModel.currentVideo.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isFullScreen by viewModel.isFullScreen.collectAsStateWithLifecycle()
    val isPlayerCollapsed by viewModel.isPlayerCollapsed.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val isContinuousPlay by viewModel.isContinuousPlay.collectAsStateWithLifecycle()
    val relatedVideos by viewModel.relatedVideos.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val isCurrentFavorite by viewModel.isCurrentFavorite.collectAsStateWithLifecycle()
    val isCurrentOfflineCached by viewModel.isCurrentOfflineCached.collectAsStateWithLifecycle()

    val trendingVideosState by viewModel.trendingVideosState.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResultsState by viewModel.searchResultsState.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()

    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    val offlineVideos by viewModel.offlineVideos.collectAsStateWithLifecycle()

    var showStripeSheet by remember { mutableStateOf(false) }
    var showDebugSheet by remember { mutableStateOf(false) }

    // Back handling
    BackHandler(enabled = currentVideo != null && !isPlayerCollapsed) {
        if (isFullScreen) {
            viewModel.setFullScreen(false)
        } else {
            viewModel.minimizePlayer()
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(DarkSurface)) {
        val isWideScreen = maxWidth >= 720.dp

        // Fullscreen Mode Override
        if (isFullScreen && currentVideo != null) {
            VideoPlayerView(
                video = currentVideo!!,
                isFullScreen = true,
                playbackSpeed = playbackSpeed,
                isContinuousPlay = isContinuousPlay,
                onToggleFullScreen = { viewModel.toggleFullScreen() },
                onSetPlaybackSpeed = { viewModel.setPlaybackSpeed(it) },
                onToggleContinuousPlay = { viewModel.toggleContinuousPlay() },
                onMinimize = { viewModel.minimizePlayer() },
                onClose = { viewModel.closePlayer() },
                onPlayNext = { viewModel.playNextRelatedVideo() },
                modifier = Modifier.fillMaxSize()
            )
            return@BoxWithConstraints
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // Adaptive Navigation Rail for Tablets / Wide screens
            if (isWideScreen) {
                NavigationRail(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    header = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(YouTubeRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                ) {
                    NavigationRailItem(
                        selected = currentTab == AppTab.HOME,
                        onClick = { viewModel.selectTab(AppTab.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = YouTubeRed,
                            selectedTextColor = YouTubeRed,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == AppTab.TRENDING,
                        onClick = { viewModel.selectTab(AppTab.TRENDING) },
                        icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = "Trending") },
                        label = { Text("Trending") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = YouTubeRed,
                            selectedTextColor = YouTubeRed,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == AppTab.LIBRARY,
                        onClick = { viewModel.selectTab(AppTab.LIBRARY) },
                        icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Library") },
                        label = { Text("Library") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = YouTubeRed,
                            selectedTextColor = YouTubeRed,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == AppTab.SETTINGS,
                        onClick = { viewModel.selectTab(AppTab.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = YouTubeRed,
                            selectedTextColor = YouTubeRed,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }

            // Main Scaffold
            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(YouTubeRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Logo",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "YouPlayer",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Online / Offline indicator dot
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isOnline) OnlineGreen else Color(0xFFF59E0B))
                                )
                            }
                        },
                        actions = {
                            if (isSupporter) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AccentGold.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("VIP", color = AccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            IconButton(
                                onClick = { viewModel.selectTab(AppTab.SEARCH) },
                                modifier = Modifier.testTag("top_search_button")
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                            }

                            IconButton(
                                onClick = { showDebugSheet = true },
                                modifier = Modifier.testTag("top_debug_button")
                            ) {
                                Icon(Icons.Default.BugReport, contentDescription = "Debug Logs", tint = TextSecondary)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = DarkSurface,
                            titleContentColor = TextPrimary
                        )
                    )
                },
                bottomBar = {
                    if (!isWideScreen) {
                        Column {
                            // Mini Player Bar docked right above bottom navigation bar
                            if (currentVideo != null && isPlayerCollapsed) {
                                MiniPlayer(
                                    video = currentVideo!!,
                                    isPlaying = isPlaying,
                                    onExpand = { viewModel.expandPlayer() },
                                    onTogglePlay = { viewModel.togglePlayPause() },
                                    onClose = { viewModel.closePlayer() }
                                )
                            }

                            NavigationBar(
                                containerColor = DarkSurface,
                                contentColor = TextPrimary
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == AppTab.HOME,
                                    onClick = { viewModel.selectTab(AppTab.HOME) },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = YouTubeRed,
                                        selectedTextColor = YouTubeRed,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary,
                                        indicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.testTag("nav_home")
                                )
                                NavigationBarItem(
                                    selected = currentTab == AppTab.TRENDING,
                                    onClick = { viewModel.selectTab(AppTab.TRENDING) },
                                    icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = "Trending") },
                                    label = { Text("Trending", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = YouTubeRed,
                                        selectedTextColor = YouTubeRed,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary,
                                        indicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.testTag("nav_trending")
                                )
                                NavigationBarItem(
                                    selected = currentTab == AppTab.LIBRARY,
                                    onClick = { viewModel.selectTab(AppTab.LIBRARY) },
                                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Library") },
                                    label = { Text("Library", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = YouTubeRed,
                                        selectedTextColor = YouTubeRed,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary,
                                        indicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.testTag("nav_library")
                                )
                                NavigationBarItem(
                                    selected = currentTab == AppTab.SETTINGS,
                                    onClick = { viewModel.selectTab(AppTab.SETTINGS) },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = YouTubeRed,
                                        selectedTextColor = YouTubeRed,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary,
                                        indicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.testTag("nav_settings")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Active Tab Screen
                    when (currentTab) {
                        AppTab.HOME, AppTab.TRENDING -> HomeScreen(
                            videosState = trendingVideosState,
                            categories = viewModel.categories,
                            selectedCategory = selectedCategory,
                            onSelectCategory = { viewModel.selectCategory(it) },
                            onVideoClick = { viewModel.playVideo(it) },
                            onRefresh = { viewModel.loadTrending(selectedCategory, forceRefresh = true) },
                            isOnline = isOnline
                        )

                        AppTab.SEARCH -> SearchScreen(
                            query = searchQuery,
                            onQueryChange = { viewModel.onSearchQueryChange(it) },
                            onSubmitSearch = { viewModel.submitSearch(it) },
                            onClearQuery = { viewModel.clearSearch() },
                            recentSearches = recentSearches,
                            onRemoveRecentSearch = { viewModel.removeRecentSearch(it) },
                            searchResultsState = searchResultsState,
                            onVideoClick = { viewModel.playVideo(it) }
                        )

                        AppTab.LIBRARY -> LibraryScreen(
                            favorites = favorites,
                            watchHistory = watchHistory,
                            offlineVideos = offlineVideos,
                            onVideoClick = { viewModel.playVideo(it) },
                            onClearHistory = { viewModel.clearWatchHistory() }
                        )

                        AppTab.SETTINGS -> SettingsScreen(
                            isOnline = isOnline,
                            isSupporter = isSupporter,
                            onOpenStripeCheckout = { showStripeSheet = true },
                            onTriggerTestNotification = { viewModel.triggerTrendingNotification() },
                            onOpenDebugLogs = { showDebugSheet = true }
                        )
                    }

                    // Full Video Player Overlay when not collapsed
                    if (currentVideo != null && !isPlayerCollapsed) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DarkSurface)
                        ) {
                                VideoPlayerView(
                                    video = currentVideo!!,
                                    isFullScreen = false,
                                    playbackSpeed = playbackSpeed,
                                    isContinuousPlay = isContinuousPlay,
                                    onToggleFullScreen = { viewModel.toggleFullScreen() },
                                    onSetPlaybackSpeed = { viewModel.setPlaybackSpeed(it) },
                                    onToggleContinuousPlay = { viewModel.toggleContinuousPlay() },
                                    onMinimize = { viewModel.minimizePlayer() },
                                    onClose = { viewModel.closePlayer() },
                                    onPlayNext = { viewModel.playNextRelatedVideo() }
                                )

                                VideoDetailView(
                                    video = currentVideo!!,
                                    relatedVideos = relatedVideos,
                                    comments = comments,
                                    isFavorite = isCurrentFavorite,
                                    isOfflineCached = isCurrentOfflineCached,
                                    onToggleFavorite = { viewModel.toggleCurrentFavorite() },
                                    onToggleOfflineCache = { viewModel.toggleCurrentOfflineCache() },
                                    onSelectRelatedVideo = { viewModel.playVideo(it) },
                                    onTipCreator = { showStripeSheet = true },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                    }
                }
            }
        }

        // Stripe Checkout Modal Bottom Sheet
        if (showStripeSheet) {
            StripeCheckoutSheet(
                paymentState = stripePaymentState,
                onDismiss = { showStripeSheet = false },
                onProcessPayment = { amount, tier ->
                    viewModel.processStripeSupporter(amount, tier)
                },
                onResetState = { viewModel.resetStripeState() }
            )
        }

        // Diagnostics Debug Log Viewer Modal Bottom Sheet
        if (showDebugSheet) {
            DebugLogSheet(
                logs = logs,
                onDismiss = { showDebugSheet = false },
                onClearLogs = { viewModel.clearDebugLogs() }
            )
        }
    }
}
