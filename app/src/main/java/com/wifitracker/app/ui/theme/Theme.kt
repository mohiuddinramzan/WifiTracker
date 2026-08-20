package com.wifitracker.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AccentCyan = Color(0xFF00E5FF)
val AccentBlue = Color(0xFF4C8DFF)
val BackgroundDark = Color(0xFF0E0F13)
val SurfaceDark = Color(0xFF191B20)
val SurfaceVariantDark = Color(0xFF23262D)
val TextPrimary = Color(0xFFF2F3F5)
val TextSecondary = Color(0xFF9AA0AC)

private val WifiTrackerColorScheme = darkColorScheme(
    primary = AccentCyan,
    secondary = AccentBlue,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

@Composable
fun WifiTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WifiTrackerColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
