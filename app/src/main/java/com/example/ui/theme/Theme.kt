package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LuminaPurple,
    secondary = LuminaMagenta,
    tertiary = LuminaCyan,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF1E2338),
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val LightColorScheme = lightColorScheme(
    primary = LuminaPurple,
    secondary = LuminaMagenta,
    tertiary = LuminaCyan,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B)
)

@Composable
fun CrexaTheme(
    forceDarkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val darkTheme = forceDarkTheme ?: isSystemInDarkTheme()
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun LuminaTheme(
    forceDarkTheme: Boolean? = null,
    content: @Composable () -> Unit
) = CrexaTheme(forceDarkTheme = forceDarkTheme, content = content)
