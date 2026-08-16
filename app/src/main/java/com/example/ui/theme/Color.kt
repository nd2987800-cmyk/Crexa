package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val CrexaPurple = Color(0xFF7C3AED)
val CrexaMagenta = Color(0xFFEC4899)
val CrexaCyan = Color(0xFF06B6D4)
val CrexaOrange = Color(0xFFF97316)

val LuminaPurple = CrexaPurple
val LuminaMagenta = CrexaMagenta
val LuminaCyan = CrexaCyan
val LuminaOrange = CrexaOrange

val LightBackground = Color(0xFFFFFFFF) // Pure White
val LightSurface = Color(0xFFFFFFFF) // Pure White
val LightOnSurface = Color(0xFF0F172A) // High-contrast dark charcoal/black text
val FeedCardBorder = Color(0xFFF1F5F9) // Subtle light-gray card border
val FeedDivider = Color(0xFFF1F5F9) // Subtle light-gray separator

val DarkBackground = Color(0xFF0B0D17)
val DarkSurface = Color(0xFF16192A)
val DarkOnSurface = Color(0xFFF1F5F9)

val StoryGradient = Brush.linearGradient(
    colors = listOf(LuminaMagenta, LuminaOrange, LuminaPurple)
)

val BrandGradient = Brush.horizontalGradient(
    colors = listOf(LuminaPurple, LuminaMagenta, LuminaCyan)
)

val DarkOverlayGradient = Brush.verticalGradient(
    colors = listOf(
        Color.Transparent,
        Color.Black.copy(alpha = 0.4f),
        Color.Black.copy(alpha = 0.85f)
    )
)
