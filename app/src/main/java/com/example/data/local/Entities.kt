package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.VideoItem

@Entity(tableName = "favorites")
data class FavoriteVideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val duration: String,
    val viewsCount: String,
    val savedAt: Long = System.currentTimeMillis()
) {
    fun toVideoItem(): VideoItem = VideoItem(
        id = id,
        title = title,
        channelTitle = channelTitle,
        thumbnailUrl = thumbnailUrl,
        duration = duration,
        viewsCount = viewsCount,
        publishedAt = "Saved"
    )
}

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val duration: String,
    val viewsCount: String,
    val watchedAt: Long = System.currentTimeMillis(),
    val progressSeconds: Int = 0
) {
    fun toVideoItem(): VideoItem = VideoItem(
        id = id,
        title = title,
        channelTitle = channelTitle,
        thumbnailUrl = thumbnailUrl,
        duration = duration,
        viewsCount = viewsCount,
        publishedAt = "Watched recently"
    )
}

@Entity(tableName = "offline_cache")
data class OfflineCacheEntity(
    @PrimaryKey val id: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val duration: String,
    val description: String,
    val cachedAt: Long = System.currentTimeMillis(),
    val fileSizeBytes: Long = 25_400_000L
) {
    fun toVideoItem(): VideoItem = VideoItem(
        id = id,
        title = title,
        channelTitle = channelTitle,
        thumbnailUrl = thumbnailUrl,
        duration = duration,
        viewsCount = "Offline Available",
        publishedAt = "Cached",
        description = description
    )
}
