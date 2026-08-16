package com.example.data.gemini

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val citations: List<String> = emptyList(),
    val isThinking: Boolean = false
)

enum class ChatRole(val displayName: String, val systemInstruction: String, val defaultModel: String, val iconName: String) {
    CREATOR_COACH(
        "Creator Coach",
        "You are an elite Instagram & social media growth coach. Help the creator craft viral reels, optimize hooks, boost engagement, increase followers, and develop content strategies. Be encouraging, actionable, and structured with clear bullet points and emojis.",
        "gemini-3.1-pro-preview",
        "school"
    ),
    VIRAL_STRATEGIST(
        "Viral Strategist",
        "You are a viral social media strategist. Analyze trends, recommend trending audio concepts, script 15-30 second reels with punchy hooks, and recommend optimal posting times.",
        "gemini-3.5-flash",
        "trending_up"
    ),
    CAPTION_WIZARD(
        "Caption & Hashtag Wizard",
        "You are a master social media copywriter. Write engaging, witty, aesthetic, or high-converting captions with optimal hashtag sets (high, medium, niche density).",
        "gemini-3.1-flash-lite-preview",
        "auto_awesome"
    ),
    PHOTO_CRITIQUE(
        "Visual & Style Critic",
        "You are a professional photography director and visual aesthetician. Critique photo composition, color grading, lighting, framing, and suggest post-processing filters.",
        "gemini-3.1-pro-preview",
        "palette"
    )
}

data class GroundedSearchResult(
    val answer: String,
    val sources: List<SearchSource> = emptyList()
)

data class SearchSource(
    val title: String,
    val url: String
)

data class GroundedMapsResult(
    val recommendations: String,
    val locations: List<MapLocationInfo> = emptyList()
)

data class MapLocationInfo(
    val name: String,
    val description: String,
    val bestTimeToShoot: String = "Golden Hour (5:30 PM)",
    val aestheticVibe: String = "Urban / Sunset"
)

data class GeneratedImageResult(
    val imageBase64: String? = null,
    val imageUrl: String? = null,
    val prompt: String,
    val size: String,
    val aspectRatio: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class GeneratedVideoResult(
    val videoUrl: String? = null,
    val thumbnailBase64: String? = null,
    val prompt: String,
    val aspectRatio: String,
    val durationSeconds: Int = 5,
    val status: String = "COMPLETED"
)

data class AiCaptionResult(
    val captions: List<String>,
    val hashtags: List<String>,
    val suggestedMusicGenre: String,
    val recommendedTime: String
)

data class PostCritiqueResult(
    val score: Int, // 0 - 100
    val verdict: String,
    val strengths: List<String>,
    val improvements: List<String>,
    val viralPotential: String
)

fun Bitmap.toJpegBase64(quality: Int = 80): String {
    val outputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
}
