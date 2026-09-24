package com.example.data.repository

import com.example.data.local.FavoriteDao
import com.example.data.local.FavoriteVideoEntity
import com.example.data.local.OfflineCacheDao
import com.example.data.local.OfflineCacheEntity
import com.example.data.local.WatchHistoryDao
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.CommentItem
import com.example.data.model.Resource
import com.example.data.model.VideoItem
import com.example.data.remote.CuratedVideoCatalog
import com.example.data.remote.PrivacyApiClient
import com.example.util.YouPlayerLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class VideoRepository(
    private val favoriteDao: FavoriteDao,
    private val historyDao: WatchHistoryDao,
    private val offlineDao: OfflineCacheDao
) {
    private val TAG = "VideoRepository"

    fun getTrending(category: String, forceRefresh: Boolean = false): Flow<Resource<List<VideoItem>>> = flow {
        emit(Resource.Loading)
        try {
            val videos = PrivacyApiClient.fetchTrending(category)
            emit(Resource.Success(videos))
        } catch (e: Exception) {
            YouPlayerLogger.e(TAG, "Error in getTrending: ${e.message}", e)
            // Fallback
            val fallback = CuratedVideoCatalog.getTrendingVideos(category)
            emit(Resource.Success(fallback))
        }
    }

    fun searchVideos(query: String): Flow<Resource<List<VideoItem>>> = flow {
        if (query.isBlank()) {
            emit(Resource.Success(emptyList()))
            return@flow
        }
        emit(Resource.Loading)
        try {
            val results = PrivacyApiClient.searchVideos(query)
            emit(Resource.Success(results))
        } catch (e: Exception) {
            YouPlayerLogger.e(TAG, "Error in searchVideos: ${e.message}", e)
            val fallback = CuratedVideoCatalog.searchCurated(query)
            emit(Resource.Success(fallback))
        }
    }

    suspend fun getRelatedVideos(videoId: String): List<VideoItem> {
        return CuratedVideoCatalog.getRelatedVideos(videoId)
    }

    suspend fun getComments(videoId: String): List<CommentItem> {
        return PrivacyApiClient.fetchComments(videoId)
    }

    // --- Favorites ---
    val favorites: Flow<List<VideoItem>> = favoriteDao.getAllFavorites().map { list ->
        list.map { it.toVideoItem() }
    }

    fun isFavorite(videoId: String): Flow<Boolean> = favoriteDao.isFavorite(videoId)

    suspend fun toggleFavorite(video: VideoItem) {
        val isFav = favoriteDao.isFavoriteSync(video.id)
        if (isFav) {
            favoriteDao.deleteById(video.id)
            YouPlayerLogger.i(TAG, "Removed from favorites: ${video.title}")
        } else {
            favoriteDao.insert(
                FavoriteVideoEntity(
                    id = video.id,
                    title = video.title,
                    channelTitle = video.channelTitle,
                    thumbnailUrl = video.thumbnailUrl,
                    duration = video.duration,
                    viewsCount = video.viewsCount
                )
            )
            YouPlayerLogger.i(TAG, "Added to favorites: ${video.title}")
        }
    }

    // --- Watch History ---
    val watchHistory: Flow<List<VideoItem>> = historyDao.getHistory().map { list ->
        list.map { it.toVideoItem() }
    }

    suspend fun recordHistory(video: VideoItem, progressSeconds: Int = 0) {
        historyDao.insert(
            WatchHistoryEntity(
                id = video.id,
                title = video.title,
                channelTitle = video.channelTitle,
                thumbnailUrl = video.thumbnailUrl,
                duration = video.duration,
                viewsCount = video.viewsCount,
                progressSeconds = progressSeconds
            )
        )
        YouPlayerLogger.d(TAG, "Saved to watch history: ${video.title}")
    }

    suspend fun clearHistory() {
        historyDao.clearAll()
        YouPlayerLogger.i(TAG, "Cleared all watch history")
    }

    // --- Offline Cache ---
    val offlineVideos: Flow<List<VideoItem>> = offlineDao.getAllCached().map { list ->
        list.map { it.toVideoItem() }
    }

    fun isOfflineCached(videoId: String): Flow<Boolean> = offlineDao.isCached(videoId)

    suspend fun cacheForOffline(video: VideoItem) {
        val entity = OfflineCacheEntity(
            id = video.id,
            title = video.title,
            channelTitle = video.channelTitle,
            thumbnailUrl = video.thumbnailUrl,
            duration = video.duration,
            description = video.description
        )
        offlineDao.insert(entity)
        YouPlayerLogger.i(TAG, "Saved to offline cache: ${video.title}")
    }

    suspend fun toggleOfflineCache(video: VideoItem) = cacheForOffline(video)

    suspend fun removeOfflineCache(videoId: String) {
        offlineDao.deleteById(videoId)
        YouPlayerLogger.i(TAG, "Removed from offline cache: $videoId")
    }
}
