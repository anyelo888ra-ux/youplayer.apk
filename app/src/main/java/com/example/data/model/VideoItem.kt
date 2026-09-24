package com.example.data.model

data class VideoItem(
    val id: String,
    val title: String,
    val channelTitle: String,
    val channelId: String = "",
    val channelAvatarUrl: String = "",
    val thumbnailUrl: String,
    val duration: String,
    val viewsCount: String,
    val publishedAt: String,
    val description: String = "",
    val isLive: Boolean = false,
    val category: String = "All"
)

data class CommentItem(
    val id: String,
    val authorName: String,
    val authorAvatar: String,
    val text: String,
    val likes: String,
    val publishedTime: String
)

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
