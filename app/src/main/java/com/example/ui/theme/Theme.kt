package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val YouPlayerDarkColorScheme = darkColorScheme(
    primary = YouTubeRed,
    onPrimary = Color.White,
    primaryContainer = YouTubeRedDark,
    onPrimaryContainer = Color.White,
    secondary = YouTubeRedLight,
    onSecondary = Color.White,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentGold,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkDivider,
    outlineVariant = Color(0xFF404040)
)

private val YouPlayerLightColorScheme = lightColorScheme(
    primary = YouTubeRed,
    onPrimary = Color.White,
    background = Color(0xFFF9F9F9),
    onBackground = Color(0xFF0F0F0F),
    surface = Color.White,
    onSurface = Color(0xFF0F0F0F),
    surfaceVariant = Color(0xFFEBEBEB),
    onSurfaceVariant = Color(0xFF606060),
    outline = Color(0xFFCCCCCC)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force modern YouTube dark theme by default
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) YouPlayerDarkColorScheme else YouPlayerLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DarkBackground.toArgb()
                window.navigationBarColor = DarkBackground.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
