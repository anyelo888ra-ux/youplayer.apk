package com.example.data.remote

import com.example.data.model.CommentItem
import com.example.data.model.VideoItem
import com.example.util.YouPlayerLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object PrivacyApiClient {
    private const val TAG = "PrivacyApiClient"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    // Public Invidious instances with open CORS/APIs
    private val INSTANCES = listOf(
        "https://invidious.nerdvpn.de",
        "https://inv.nadeko.net",
        "https://invidious.drgns.space",
        "https://yt.artemislena.eu"
    )

    private var currentInstanceIndex = 0

    private fun getCurrentBaseUrl(): String {
        return INSTANCES[currentInstanceIndex % INSTANCES.size]
    }

    private fun rotateInstance() {
        currentInstanceIndex = (currentInstanceIndex + 1) % INSTANCES.size
        YouPlayerLogger.i(TAG, "Rotated to instance: ${getCurrentBaseUrl()}")
    }

    suspend fun fetchTrending(category: String): List<VideoItem> = withContext(Dispatchers.IO) {
        val attempts = INSTANCES.size
        for (i in 0 until attempts) {
            val base = getCurrentBaseUrl()
            val url = "$base/api/v1/trending?region=US"
            try {
                YouPlayerLogger.d(TAG, "Fetching trending from: $url (category: $category)")
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "YouPlayer/1.0 (Android; Privacy-Oriented)")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrBlank()) {
                            val items = parseInvidiousVideos(body, category)
                            if (items.isNotEmpty()) {
                                YouPlayerLogger.i(TAG, "Successfully fetched ${items.size} trending videos")
                                return@withContext items
                            }
                        }
                    } else {
                        YouPlayerLogger.w(TAG, "HTTP error ${response.code} from $base")
                    }
                }
            } catch (e: Exception) {
                YouPlayerLogger.w(TAG, "Instance $base failed: ${e.message}")
            }
            rotateInstance()
        }

        // Resilient fallback to curated trending catalog
        YouPlayerLogger.i(TAG, "Using curated offline-resilient trending videos for category: $category")
        return@withContext CuratedVideoCatalog.getTrendingVideos(category)
    }

    suspend fun searchVideos(query: String): List<VideoItem> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val attempts = INSTANCES.size
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")

        for (i in 0 until attempts) {
            val base = getCurrentBaseUrl()
            val url = "$base/api/v1/search?q=$encodedQuery&type=video"
            try {
                YouPlayerLogger.d(TAG, "Searching from: $url")
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "YouPlayer/1.0 (Android; Privacy-Oriented)")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrBlank()) {
                            val items = parseInvidiousVideos(body, "Search")
                            if (items.isNotEmpty()) {
                                YouPlayerLogger.i(TAG, "Successfully found ${items.size} search results for '$query'")
                                return@withContext items
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                YouPlayerLogger.w(TAG, "Search instance $base error: ${e.message}")
            }
            rotateInstance()
        }

        // Fallback filter from curated database for seamless user experience
        return@withContext CuratedVideoCatalog.searchCurated(query)
    }

    suspend fun fetchComments(videoId: String): List<CommentItem> = withContext(Dispatchers.IO) {
        val attempts = 2
        for (i in 0 until attempts) {
            val base = getCurrentBaseUrl()
            val url = "$base/api/v1/comments/$videoId"
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "YouPlayer/1.0")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrBlank()) {
                            val comments = parseComments(body)
                            if (comments.isNotEmpty()) return@withContext comments
                        }
                    }
                }
            } catch (e: Exception) {
                YouPlayerLogger.w(TAG, "Comments fetch error: ${e.message}")
            }
            rotateInstance()
        }
        return@withContext CuratedVideoCatalog.getSampleComments(videoId)
    }

    private fun parseInvidiousVideos(jsonStr: String, fallbackCategory: String): List<VideoItem> {
        val list = mutableListOf<VideoItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val type = obj.optString("type", "video")
                if (type == "video" || !obj.has("type")) {
                    val id = obj.optString("videoId").ifBlank { obj.optString("id") }
                    val title = obj.optString("title", "Untitled Video")
                    val author = obj.optString("author", "Creator")
                    val authorId = obj.optString("authorId", "")
                    val viewCount = obj.optLong("viewCount", 0L)
                    val lengthSeconds = obj.optInt("lengthSeconds", 0)
                    val publishedText = obj.optString("publishedText", "Recently")
                    val description = obj.optString("description", "")
                    val isLive = obj.optBoolean("liveNow", false)

                    if (id.isNotBlank() && title.isNotBlank()) {
                        val thumb = "https://i.ytimg.com/vi/$id/hqdefault.jpg"
                        list.add(
                            VideoItem(
                                id = id,
                                title = title,
                                channelTitle = author,
                                channelId = authorId,
                                channelAvatarUrl = "https://api.dicebear.com/7.x/identicon/png?seed=$author",
                                thumbnailUrl = thumb,
                                duration = formatSeconds(lengthSeconds),
                                viewsCount = formatViews(viewCount),
                                publishedAt = publishedText,
                                description = description,
                                isLive = isLive,
                                category = fallbackCategory
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            YouPlayerLogger.e(TAG, "Error parsing Invidious JSON", e)
        }
        return list
    }

    private fun parseComments(jsonStr: String): List<CommentItem> {
        val list = mutableListOf<CommentItem>()
        try {
            val json = JSONObject(jsonStr)
            val commentsArray = json.optJSONArray("comments") ?: JSONArray()
            for (i in 0 until commentsArray.length()) {
                val obj = commentsArray.getJSONObject(i)
                val id = obj.optString("commentId", "c_$i")
                val author = obj.optString("author", "User")
                val content = obj.optString("content", "")
                val likeCount = obj.optInt("likeCount", 0)
                val publishedTime = obj.optString("publishedText", "Just now")
                list.add(
                    CommentItem(
                        id = id,
                        authorName = author,
                        authorAvatar = "https://api.dicebear.com/7.x/bottts/png?seed=$author",
                        text = content,
                        likes = if (likeCount > 0) "$likeCount" else "0",
                        publishedTime = publishedTime
                    )
                )
            }
        } catch (e: Exception) {
            YouPlayerLogger.e(TAG, "Error parsing comments", e)
        }
        return list
    }

    fun formatSeconds(seconds: Int): String {
        if (seconds <= 0) return "LIVE"
        val m = seconds / 60
        val s = seconds % 60
        val h = m / 60
        return if (h > 0) {
            String.format("%d:%02d:%02d", h, m % 60, s)
        } else {
            String.format("%d:%02d", m, s)
        }
    }

    fun formatViews(views: Long): String {
        return when {
            views >= 1_000_000_000 -> String.format("%.1fB views", views / 1_000_000_000.0)
            views >= 1_000_000 -> String.format("%.1fM views", views / 1_000_000.0)
            views >= 1_000 -> String.format("%.1fK views", views / 1_000.0)
            views > 0 -> "$views views"
            else -> "Recommended"
        }
    }
}
