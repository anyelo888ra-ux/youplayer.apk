package com.example.ui.components

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.VideoItem
import com.example.ui.theme.YouTubeRed
import com.example.util.YouPlayerLogger
import kotlinx.coroutines.delay

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoPlayerView(
    video: VideoItem,
    isFullScreen: Boolean,
    playbackSpeed: Float,
    isContinuousPlay: Boolean,
    onToggleFullScreen: () -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit,
    onToggleContinuousPlay: () -> Unit,
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    onPlayNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var showControls by remember { mutableStateOf(true) }
    var speedMenuOpen by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Auto-hide controls after 4 seconds
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(4500)
            showControls = false
        }
    }

    // Fullscreen screen orientation handling
    DisposableEffect(isFullScreen) {
        if (isFullScreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Inject playback speed when changed
    LaunchedEffect(playbackSpeed, webViewRef) {
        webViewRef?.evaluateJavascript(
            "var iframe = document.getElementById('player'); if (iframe && iframe.contentWindow) { iframe.contentWindow.postMessage('{\"event\":\"command\",\"func\":\"setPlaybackRate\",\"args\":[$playbackSpeed]}', '*'); }",
            null
        )
    }

    val boxModifier = if (isFullScreen) {
        modifier.fillMaxSize().background(Color.Black)
    } else {
        modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Black)
    }

    Box(
        modifier = boxModifier
            .testTag("video_player_box")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
    ) {
        // Embedded HTML5/iFrame Video Player
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.BLACK)
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            YouPlayerLogger.d("VideoPlayer", "Page finished loading for video ${video.id}")
                        }
                    }
                    webViewRef = this
                }
            },
            update = { wv ->
                val embedHtml = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                    <style>
                    * { margin:0; padding:0; box-sizing:border-box; }
                    html, body { width:100%; height:100%; background:#000; overflow:hidden; }
                    iframe { width:100%; height:100%; border:none; display:block; }
                    </style>
                    </head>
                    <body>
                    <iframe id="player"
                        src="https://www.youtube-nocookie.com/embed/${video.id}?autoplay=1&enablejsapi=1&playsinline=1&fs=1&rel=0&iv_load_policy=3&modestbranding=1"
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                        allowfullscreen>
                    </iframe>
                    </body>
                    </html>
                """.trimIndent()
                wv.loadDataWithBaseURL("https://www.youtube-nocookie.com", embedHtml, "text/html", "UTF-8", null)
            }
        )

        // Custom Overlay Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                // Top control bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isFullScreen) {
                        IconButton(
                            onClick = onMinimize,
                            modifier = Modifier.size(40.dp).testTag("minimize_player_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Minimize Player",
                                tint = Color.White
                            )
                        }
                    }

                    Text(
                        text = video.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(40.dp).testTag("close_player_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Player",
                            tint = Color.White
                        )
                    }
                }

                // Bottom control bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Continuous play & Autoplay switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.background(
                            Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp)
                        ).padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Autoplay",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = isContinuousPlay,
                            onCheckedChange = { onToggleContinuousPlay() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = YouTubeRed,
                                checkedTrackColor = YouTubeRed.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.size(width = 38.dp, height = 24.dp)
                        )
                    }

                    // Next / Speed / Fullscreen controls
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Skip next video
                        IconButton(
                            onClick = onPlayNext,
                            modifier = Modifier.size(40.dp).testTag("next_video_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Video",
                                tint = Color.White
                            )
                        }

                        // Speed selector
                        Box {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier.clickable { speedMenuOpen = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Playback Speed",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${playbackSpeed}x",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = speedMenuOpen,
                                onDismissRequest = { speedMenuOpen = false }
                            ) {
                                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (speed == 1.0f) "1.0x (Normal)" else "${speed}x",
                                                fontWeight = if (speed == playbackSpeed) FontWeight.Bold else FontWeight.Normal,
                                                color = if (speed == playbackSpeed) YouTubeRed else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            onSetPlaybackSpeed(speed)
                                            speedMenuOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Fullscreen Toggle
                        IconButton(
                            onClick = onToggleFullScreen,
                            modifier = Modifier.size(40.dp).testTag("fullscreen_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = if (isFullScreen) "Exit Fullscreen" else "Fullscreen",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
