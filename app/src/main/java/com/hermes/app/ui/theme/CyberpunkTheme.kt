package com.hermes.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF0A0E17)
val SurfaceDark = Color(0xFF121829)
val SurfaceVariantDark = Color(0xFF1C2640)

val NeonCyan = Color(0xFF00F0FF)
val NeonMagenta = Color(0xFFE040FB)
val NeonGreen = Color(0xFF39FF14)
val NeonAmber = Color(0xFFFFAB00)
val NeonRed = Color(0xFFFF1744)

val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)

private val CyberpunkDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003847),
    onPrimaryContainer = NeonCyan,

    secondary = NeonMagenta,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF4A005C),
    onSecondaryContainer = NeonMagenta,

    tertiary = NeonGreen,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF004D1A),
    onTertiaryContainer = NeonGreen,

    background = DarkBackground,
    onBackground = TextPrimary,

    surface = SurfaceDark,
    onSurface = TextPrimary,

    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,

    error = NeonRed,
    onError = Color.Black,
)

@Composable
fun CyberpunkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CyberpunkDarkColorScheme,
        content = content
    )
}
