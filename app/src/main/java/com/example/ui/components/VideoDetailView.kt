package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.CommentItem
import com.example.data.model.VideoItem
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DarkDivider
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.YouTubeRed

@Composable
fun VideoDetailView(
    video: VideoItem,
    relatedVideos: List<VideoItem>,
    comments: List<CommentItem>,
    isFavorite: Boolean,
    isOfflineCached: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleOfflineCache: () -> Unit,
    onSelectRelatedVideo: (VideoItem) -> Unit,
    onTipCreator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSubscribed by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }
    var isDescExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Up Next, 1: Comments

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Video Header Info
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = video.title,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${video.viewsCount} • ${video.publishedAt}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Channel Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(video.channelAvatarUrl.ifBlank { "https://api.dicebear.com/7.x/identicon/png?seed=${video.channelTitle}" })
                            .crossfade(true)
                            .build(),
                        contentDescription = video.channelTitle,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.channelTitle,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isSubscribed) "Subscribed (Notifications On)" else "Privacy-friendly channel",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { isSubscribed = !isSubscribed },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSubscribed) DarkSurfaceVariant else Color.White,
                            contentColor = if (isSubscribed) TextPrimary else Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp).testTag("subscribe_button")
                    ) {
                        Text(
                            text = if (isSubscribed) "Subscribed" else "Subscribe",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons Row (Like, Favorite, Offline, Share, Tip)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        ActionChip(
                            icon = if (isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                            label = if (isLiked) "Liked" else "Like",
                            isActive = isLiked,
                            activeColor = YouTubeRed,
                            onClick = { isLiked = !isLiked }
                        )
                    }

                    item {
                        ActionChip(
                            icon = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            label = if (isFavorite) "Saved" else "Save",
                            isActive = isFavorite,
                            activeColor = AccentGold,
                            onClick = onToggleFavorite
                        )
                    }

                    item {
                        ActionChip(
                            icon = if (isOfflineCached) Icons.Default.DownloadDone else Icons.Default.Download,
                            label = if (isOfflineCached) "Cached" else "Download",
                            isActive = isOfflineCached,
                            activeColor = Color(0xFF22C55E),
                            onClick = onToggleOfflineCache
                        )
                    }

                    item {
                        ActionChip(
                            icon = Icons.Default.Share,
                            label = "Share",
                            onClick = {
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(
                                        android.content.Intent.EXTRA_TEXT,
                                        "Watch ${video.title} on YouPlayer: https://www.youtube.com/watch?v=${video.id}"
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Video"))
                            }
                        )
                    }

                    item {
                        ActionChip(
                            icon = Icons.Default.Star,
                            label = "Supporter Tip",
                            isActive = true,
                            activeColor = AccentGold,
                            onClick = onTipCreator
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Expandable Description Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isDescExpanded = !isDescExpanded }
                        .animateContentSize(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Description",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = if (isDescExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = video.description.ifBlank {
                                "Playing in pristine high definition on YouPlayer. Zero tracking, zero telemetry, full privacy."
                            },
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = if (isDescExpanded) 50 else 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: Up Next vs Comments
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = TextPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = YouTubeRed
                        )
                    },
                    divider = { Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkDivider)) }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Up Next (${relatedVideos.size})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Comments (${comments.size})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // Tab Content: Up Next related videos
        if (selectedTab == 0) {
            items(relatedVideos, key = { it.id }) { related ->
                CompactVideoRow(
                    video = related,
                    onClick = { onSelectRelatedVideo(related) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        } else {
            // Comments tab
            items(comments, key = { it.id }) { comment ->
                CommentRow(
                    comment = comment,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    activeColor: Color = YouTubeRed
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isActive) activeColor.copy(alpha = 0.2f) else DarkSurfaceVariant,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else TextPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isActive) activeColor else TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CompactVideoRow(
    video: VideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(video.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = video.title,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = video.duration,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${video.channelTitle} • ${video.viewsCount}",
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CommentRow(
    comment: CommentItem,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(comment.authorAvatar)
                .crossfade(true)
                .build(),
            contentDescription = comment.authorName,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.authorName,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = comment.publishedTime,
                    color = TextTertiary,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = comment.text,
                color = TextPrimary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "Likes",
                    tint = TextSecondary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = comment.likes,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}
