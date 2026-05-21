package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CosmicDarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = CosmicBlack,
    secondary = CyberBlue,
    onSecondary = TextPrimary,
    tertiary = DeepIndigoAccent,
    background = CosmicBlack,
    onBackground = TextPrimary,
    surface = DarkGreySurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateBorder,
    onSurfaceVariant = TextSecondary,
    outline = SlateBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark mode as requested by prompt
    dynamicColor: Boolean = false, // Disable to preserve obsidian sci-fi branding
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CosmicDarkColorScheme,
        typography = Typography,
        content = content
    )
}
