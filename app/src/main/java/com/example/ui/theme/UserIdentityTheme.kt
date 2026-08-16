package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

data class UserIdentityColor(
    val id: String,
    val name: String,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val ringGradient: Brush,
    val subtleBackground: Color
)

object UserThemeManager {
    // 12 Distinct, vibrant, luxury color palettes for user identity rings
    val IdentityPalettes = listOf(
        UserIdentityColor(
            id = "electric_violet",
            name = "Electric Violet",
            primary = Color(0xFF8B5CF6),
            secondary = Color(0xFFEC4899),
            accent = Color(0xFFA855F7),
            ringGradient = Brush.sweepGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF3B82F6), Color(0xFF8B5CF6))),
            subtleBackground = Color(0xFFF5F3FF)
        ),
        UserIdentityColor(
            id = "sunset_coral",
            name = "Sunset Coral",
            primary = Color(0xFFFF5E62),
            secondary = Color(0xFFFF9966),
            accent = Color(0xFFF97316),
            ringGradient = Brush.sweepGradient(listOf(Color(0xFFFF5E62), Color(0xFFFF9966), Color(0xFFFFD166), Color(0xFFFF5E62))),
            subtleBackground = Color(0xFFFFF7ED)
        ),
        UserIdentityColor(
            id = "neon_cyan",
            name = "Neon Cyan",
            primary = Color(0xFF06B6D4),
            secondary = Color(0xFF3B82F6),
            accent = Color(0xFF0EA5E9),
            ringGradient = Brush.sweepGradient(listOf(Color(0xFF06B6D4), Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF06B6D4))),
            subtleBackground = Color(0xFFECFEFF)
        ),
        UserIdentityColor(
            id = "emerald_mint",
            name = "Emerald Mint",
            primary = Color(0xFF10B981),
            secondary = Color(0xFF14B8A6),
            accent = Color(0xFF059669),
            ringGradient = Brush.sweepGradient(listOf(Color(0xFF10B981), Color(0xFF34D399), Color(0xFF06B6D4), Color(0xFF10B981))),
            subtleBackground = Color(0xFFECFDF5)
        ),
        UserIdentityColor(
            id = "vivid_fuchsia",
            name = "Vivid Fuchsia",
            primary = Color(0xFFD946EF),
            secondary = Color(0xFFF43F5E),
            accent = Color(0xFFE11D48),
            ringGradient = Brush.sweepGradient(listOf(Color(0xFFD946EF), Color(0xFFF43F5E), Color(0xFFFB7185), Color(0xFFD946EF))),
            subtleBackground = Color(0xFFFDF2F8)
        ),
        UserIdentityColor(
            id = "solar_amber",
            name = "Solar Amber",
            primary = Color(0xFFF59E0B),
            secondary = Color(0xFFEF4444),
            accent = Color(0xFFD97706),
            ringGradient = Brush.sweepGradient(listOf(Color(0xFFF59E0B), Color(0xFFF97316), Color(0xFFEF4444), Color(0xFFF59E0B))),
            subtleBackground = Color(0xFFFFFBEB)
        ),
        UserIdentityColor(
            id = "royal_indigo",
            name = "Royal Indigo",
            primary = Color(0xFF6366F1),
            secondary = Color(0xFF8B5CF6),
            accent = Color(0xFF4F46E5),
            ringGradient = Brush.sweepGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFA855F7), Color(0xFF6366F1))),
            subtleBackground = Color(0xFFEEF2FF)
        ),
        UserIdentityColor(
            id = "cyber_lime",
            name = "Cyber Lime",
            primary = Color(0xFF84CC16),
            secondary = Color(0xFF10B981),
            accent = Color(0xFF65A30D),
            ringGradient = Brush.sweepGradient(listOf(Color(0xFF84CC16), Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF84CC16))),
            subtleBackground = Color(0xFFF7FEE7)
        ),
        UserIdentityColor(
            id = "deep_crimson",
            name = "Deep Crimson",
            primary = Color(0xFFE11D48),
            secondary = Color(0xFF9333EA),
            accent = Color(0xFFBE123C),
            ringGradient = Brush.sweepGradient(listOf(Color(0xFFE11D48), Color(0xFFC026D3), Color(0xFF7C3AED), Color(0xFFE11D48))),
            subtleBackground = Color(0xFFFFF1F2)
        ),
        UserIdentityColor(
            id = "ocean_azure",
            name = "Ocean Azure",
            primary = Color(0xFF0284C7),
            secondary = Color(0xFF0D9488),
            accent = Color(0xFF0369A1),
            ringGradient = Brush.sweepGradient(listOf(Color(0xFF0284C7), Color(0xFF06B6D4), Color(0xFF14B8A6), Color(0xFF0284C7))),
            subtleBackground = Color(0xFFF0F9FF)
        ),
        UserIdentityColor(
            id = "tropical_magenta",
            name = "Tropical Magenta",
            primary = Color(0xFFF43F5E),
            secondary = Color(0xFFFB923C),
            accent = Color(0xFFE11D48),
            ringGradient = Brush.sweepGradient(listOf(Color(0xFFF43F5E), Color(0xFFFB923C), Color(0xFFFBBF24), Color(0xFFF43F5E))),
            subtleBackground = Color(0xFFFFF1F2)
        ),
        UserIdentityColor(
            id = "cosmic_purple",
            name = "Cosmic Purple",
            primary = Color(0xFF7C3AED),
            secondary = Color(0xFF06B6D4),
            accent = Color(0xFF6D28D9),
            ringGradient = Brush.sweepGradient(listOf(Color(0xFF7C3AED), Color(0xFFEC4899), Color(0xFF06B6D4), Color(0xFF7C3AED))),
            subtleBackground = Color(0xFFF3E8FF)
        )
    )

    /**
     * Deterministically obtains the distinct identity color theme for a user based on their ID or username.
     * Never changes randomly on feed refresh!
     */
    fun getColorForUser(userId: String, username: String = ""): UserIdentityColor {
        val seedString = when {
            userId.isNotBlank() -> userId.lowercase()
            username.isNotBlank() -> username.lowercase()
            else -> "crexa_default_user"
        }
        val hash = abs(seedString.hashCode())
        val index = hash % IdentityPalettes.size
        return IdentityPalettes[index]
    }
}
