package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.ui.components.CompactVideoRow
import com.example.ui.components.VideoCard
import com.example.ui.theme.DarkDivider
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

@Composable
fun LibraryScreen(
    favorites: List<VideoItem>,
    watchHistory: List<VideoItem>,
    offlineVideos: List<VideoItem>,
    onVideoClick: (VideoItem) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Favorites, 1: History, 2: Offline

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Privacy Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Privacy",
                    tint = YouTubeRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "100% Local & Private Storage",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your bookmarks, history, and offline clips stay on your device without login or tracking.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Section Tabs
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = DarkSurface,
            contentColor = TextPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                    color = YouTubeRed
                )
            },
            divider = { Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkDivider)) }
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Saved (${favorites.size})", fontSize = 12.sp)
                    }
                }
            )

            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("History (${watchHistory.size})", fontSize = 12.sp)
                    }
                }
            )

            Tab(
                selected = selectedSection == 2,
                onClick = { selectedSection = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DownloadDone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Offline (${offlineVideos.size})", fontSize = 12.sp)
                    }
                }
            )
        }

        // Content
        when (selectedSection) {
            0 -> {
                // Favorites
                if (favorites.isEmpty()) {
                    EmptySectionPlaceholder(
                        title = "No Saved Favorites Yet",
                        subtitle = "Tap the 'Save' bookmark button under any video to keep it in your local library."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(favorites, key = { it.id }) { video ->
                            VideoCard(
                                video = video,
                                onClick = { onVideoClick(video) },
                                isFavorite = true
                            )
                        }
                    }
                }
            }

            1 -> {
                // History
                if (watchHistory.isEmpty()) {
                    EmptySectionPlaceholder(
                        title = "Watch History is Empty",
                        subtitle = "Videos you watch will be recorded locally for seamless resume."
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recently Watched",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            TextButton(
                                onClick = onClearHistory,
                                modifier = Modifier.testTag("clear_history_button")
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = YouTubeRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear All", color = YouTubeRed, fontSize = 12.sp)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(watchHistory, key = { it.id }) { video ->
                                CompactVideoRow(
                                    video = video,
                                    onClick = { onVideoClick(video) }
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // Offline
                if (offlineVideos.isEmpty()) {
                    EmptySectionPlaceholder(
                        title = "No Offline Cached Videos",
                        subtitle = "Download or cache videos from the video details page to watch without internet."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(offlineVideos, key = { it.id }) { video ->
                            VideoCard(
                                video = video,
                                onClick = { onVideoClick(video) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySectionPlaceholder(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
