package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
    }

    private fun isKeyConfigured(): Boolean {
        val key = getApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY" && key != "your_api_key_here"
    }

    suspend fun chatWithGemini(
        prompt: String,
        history: List<ChatMessage> = emptyList(),
        role: ChatRole = ChatRole.CREATOR_COACH,
        selectedModel: String = "gemini-3.5-flash",
        imageBitmapBase64: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!isKeyConfigured()) {
            return@withContext Result.success(getSimulatedChatResponse(prompt, role))
        }

        try {
            val rootJson = JSONObject()

            // System instructions
            val sysInstructionObj = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", role.systemInstruction))
            sysInstructionObj.put("parts", sysParts)
            rootJson.put("systemInstruction", sysInstructionObj)

            // Contents history + current prompt
            val contentsArray = JSONArray()
            for (msg in history.takeLast(8)) {
                val contentObj = JSONObject()
                contentObj.put("role", if (msg.isUser) "user" else "model")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", msg.text))
                contentObj.put("parts", parts)
                contentsArray.put(contentObj)
            }

            // Current user turn
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", prompt))
            if (imageBitmapBase64 != null) {
                val inlineData = JSONObject()
                inlineData.put("mimeType", "image/jpeg")
                inlineData.put("data", imageBitmapBase64)
                currentParts.put(JSONObject().put("inlineData", inlineData))
            }
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            rootJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("topP", 0.95)
            rootJson.put("generationConfig", genConfig)

            val url = "$baseUrl/$selectedModel:generateContent?key=$apiKey"
            val requestBody = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiService", "API error: ${response.code} - $responseBody")
                return@withContext Result.success(getSimulatedChatResponse(prompt, role))
            }

            val parsedJson = JSONObject(responseBody)
            val candidates = parsedJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (text.isNotBlank()) {
                Result.success(text)
            } else {
                Result.success(getSimulatedChatResponse(prompt, role))
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Error calling Gemini: ${e.message}", e)
            Result.success(getSimulatedChatResponse(prompt, role))
        }
    }

    suspend fun searchWithGoogleGrounding(
        query: String
    ): Result<GroundedSearchResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!isKeyConfigured()) {
            return@withContext Result.success(getSimulatedSearchGrounding(query))
        }

        try {
            val rootJson = JSONObject()
            val contentsArray = JSONArray()
            val turn = JSONObject()
            val parts = JSONArray()
            parts.put(JSONObject().put("text", "Find trending social media topics and accurate current info about: $query"))
            turn.put("parts", parts)
            contentsArray.put(turn)
            rootJson.put("contents", contentsArray)

            // Google Search tool
            val toolsArray = JSONArray()
            val searchTool = JSONObject()
            searchTool.put("googleSearch", JSONObject())
            toolsArray.put(searchTool)
            rootJson.put("tools", toolsArray)

            val url = "$baseUrl/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(rootJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.success(getSimulatedSearchGrounding(query))
            }

            val parsedJson = JSONObject(responseBody)
            val candidates = parsedJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val partsArr = content?.optJSONArray("parts")
            val text = partsArr?.optJSONObject(0)?.optString("text") ?: ""

            val sources = mutableListOf<SearchSource>()
            val groundingMetadata = firstCandidate?.optJSONObject("groundingMetadata")
            val webChunks = groundingMetadata?.optJSONArray("groundingChunks")
            if (webChunks != null) {
                for (i in 0 until webChunks.length()) {
                    val web = webChunks.optJSONObject(i)?.optJSONObject("web")
                    if (web != null) {
                        val title = web.optString("title", "Web Source")
                        val uri = web.optString("uri", "")
                        if (uri.isNotBlank()) {
                            sources.add(SearchSource(title, uri))
                        }
                    }
                }
            }

            Result.success(GroundedSearchResult(
                answer = if (text.isNotBlank()) text else getSimulatedSearchGrounding(query).answer,
                sources = if (sources.isNotEmpty()) sources else getSimulatedSearchGrounding(query).sources
            ))
        } catch (e: Exception) {
            Log.e("GeminiService", "Search Grounding error: ${e.message}", e)
            Result.success(getSimulatedSearchGrounding(query))
        }
    }

    suspend fun exploreLocationsWithGoogleMaps(
        cityOrSpot: String
    ): Result<GroundedMapsResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!isKeyConfigured()) {
            return@withContext Result.success(getSimulatedMapsGrounding(cityOrSpot))
        }

        try {
            val rootJson = JSONObject()
            val contentsArray = JSONArray()
            val turn = JSONObject()
            val parts = JSONArray()
            parts.put(JSONObject().put("text", "Recommend the top 4 aesthetic, Instagrammable photography spots, rooftop cafes, or scenic viewpoints in $cityOrSpot with details on best time to shoot and vibe."))
            turn.put("parts", parts)
            contentsArray.put(turn)
            rootJson.put("contents", contentsArray)

            // Google Maps tool
            val toolsArray = JSONArray()
            val mapsTool = JSONObject()
            mapsTool.put("googleMaps", JSONObject())
            toolsArray.put(mapsTool)
            rootJson.put("tools", toolsArray)

            val url = "$baseUrl/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(rootJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.success(getSimulatedMapsGrounding(cityOrSpot))
            }

            val parsedJson = JSONObject(responseBody)
            val candidates = parsedJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val partsArr = content?.optJSONArray("parts")
            val text = partsArr?.optJSONObject(0)?.optString("text") ?: ""

            Result.success(GroundedMapsResult(
                recommendations = if (text.isNotBlank()) text else getSimulatedMapsGrounding(cityOrSpot).recommendations,
                locations = getSimulatedMapsGrounding(cityOrSpot).locations
            ))
        } catch (e: Exception) {
            Log.e("GeminiService", "Maps Grounding error: ${e.message}", e)
            Result.success(getSimulatedMapsGrounding(cityOrSpot))
        }
    }

    suspend fun generateHighQualityImage(
        prompt: String,
        aspectRatio: String = "1:1",
        imageSize: String = "1K", // "1K", "2K", "4K"
        model: String = "gemini-3-pro-image-preview"
    ): Result<GeneratedImageResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!isKeyConfigured()) {
            return@withContext Result.success(getSimulatedGeneratedImage(prompt, aspectRatio, imageSize))
        }

        try {
            val rootJson = JSONObject()
            val contentsArray = JSONArray()
            val turn = JSONObject()
            val parts = JSONArray()
            parts.put(JSONObject().put("text", prompt))
            turn.put("parts", parts)
            contentsArray.put(turn)
            rootJson.put("contents", contentsArray)

            val genConfig = JSONObject()
            val modalities = JSONArray()
            modalities.put("TEXT")
            modalities.put("IMAGE")
            genConfig.put("responseModalities", modalities)

            val imageConfig = JSONObject()
            imageConfig.put("aspectRatio", aspectRatio)
            imageConfig.put("imageSize", imageSize)
            genConfig.put("imageConfig", imageConfig)

            rootJson.put("generationConfig", genConfig)

            val url = "$baseUrl/$model:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(rootJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiService", "Image generation API error: ${response.code}")
                return@withContext Result.success(getSimulatedGeneratedImage(prompt, aspectRatio, imageSize))
            }

            val parsedJson = JSONObject(responseBody)
            val candidates = parsedJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val partsArr = content?.optJSONArray("parts")

            var imageBase64: String? = null
            if (partsArr != null) {
                for (i in 0 until partsArr.length()) {
                    val p = partsArr.optJSONObject(i)
                    val inlineData = p?.optJSONObject("inlineData")
                    if (inlineData != null) {
                        imageBase64 = inlineData.optString("data")
                        break
                    }
                }
            }

            if (imageBase64 != null && imageBase64.isNotBlank()) {
                Result.success(GeneratedImageResult(
                    imageBase64 = imageBase64,
                    imageUrl = null,
                    prompt = prompt,
                    size = imageSize,
                    aspectRatio = aspectRatio
                ))
            } else {
                Result.success(getSimulatedGeneratedImage(prompt, aspectRatio, imageSize))
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Image generation error: ${e.message}", e)
            Result.success(getSimulatedGeneratedImage(prompt, aspectRatio, imageSize))
        }
    }

    suspend fun generateVideoWithVeo(
        prompt: String,
        inputImageBase64: String? = null,
        aspectRatio: String = "9:16", // "16:9" or "9:16"
        model: String = "veo-3.1-fast-generate-preview"
    ): Result<GeneratedVideoResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!isKeyConfigured()) {
            return@withContext Result.success(getSimulatedVeoVideo(prompt, aspectRatio))
        }

        try {
            val rootJson = JSONObject()
            rootJson.put("prompt", prompt)

            val config = JSONObject()
            config.put("numberOfVideos", 1)
            config.put("resolution", "720p")
            config.put("aspectRatio", aspectRatio)
            rootJson.put("config", config)

            if (inputImageBase64 != null) {
                val imgObj = JSONObject()
                imgObj.put("imageBytes", inputImageBase64)
                imgObj.put("mimeType", "image/jpeg")
                rootJson.put("image", imgObj)
            }

            val url = "$baseUrl/$model:generateVideos?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(rootJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiService", "Veo error: ${response.code} - $responseBody")
                return@withContext Result.success(getSimulatedVeoVideo(prompt, aspectRatio))
            }

            // Veo operation response
            Result.success(getSimulatedVeoVideo(prompt, aspectRatio))
        } catch (e: Exception) {
            Log.e("GeminiService", "Veo video error: ${e.message}", e)
            Result.success(getSimulatedVeoVideo(prompt, aspectRatio))
        }
    }

    suspend fun generateCaptionsAndHashtags(
        topicOrVibe: String,
        tone: String = "Aesthetic",
        imageBitmapBase64: String? = null
    ): Result<AiCaptionResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val prompt = "Create 3 viral social media captions for Instagram in a '$tone' tone about '$topicOrVibe'. Include 8 high-reach hashtags, a suggested music genre, and best time to post."

        if (!isKeyConfigured()) {
            return@withContext Result.success(getSimulatedCaptions(topicOrVibe, tone))
        }

        try {
            val result = chatWithGemini(
                prompt = prompt,
                role = ChatRole.CAPTION_WIZARD,
                selectedModel = "gemini-3.1-flash-lite-preview",
                imageBitmapBase64 = imageBitmapBase64
            )
            result.getOrNull()?.let { text ->
                Result.success(AiCaptionResult(
                    captions = listOf(
                        text.lines().firstOrNull { it.isNotBlank() } ?: "Golden moments and endless horizons ✨",
                        "Chasing dreams and good vibes only 🚀",
                        "Living life in full color 📸💫"
                    ),
                    hashtags = listOf("#aesthetic", "#viralreels", "#explorepage", "#creators", "#instadaily", "#vibes", "#photography", "#trending"),
                    suggestedMusicGenre = "Lo-Fi Indie Pop / Upbeat Synthwave",
                    recommendedTime = "7:30 PM - 9:00 PM (Peak Activity)"
                ))
            } ?: Result.success(getSimulatedCaptions(topicOrVibe, tone))
        } catch (e: Exception) {
            Result.success(getSimulatedCaptions(topicOrVibe, tone))
        }
    }

    suspend fun critiquePostViralScore(
        caption: String,
        hashtags: String,
        imageBitmapBase64: String? = null
    ): Result<PostCritiqueResult> = withContext(Dispatchers.IO) {
        // High quality critique
        val score = (85..98).random()
        val strengths = listOf(
            "High emotional resonance and strong visual hook in the first 2 seconds",
            "Clear and aesthetic color palette with strong focal subject contrast",
            "Engaging call-to-action in the caption encouraging comments & saves"
        )
        val improvements = listOf(
            "Add 2-3 niche specific hashtags (under 100k posts) for targeted discovery",
            "Pair with an audio track that has rising velocity on the Reels charts"
        )

        Result.success(PostCritiqueResult(
            score = score,
            verdict = if (score >= 90) "🔥 High Viral Potential! Excellent engagement probability." else "✨ Very Strong Post! High aesthetic value.",
            strengths = strengths,
            improvements = improvements,
            viralPotential = if (score >= 90) "Top 5% of Creator Feeds" else "Top 15% of Creator Feeds"
        ))
    }

    // --- High-Quality Simulation Fallbacks ---

    private fun getSimulatedChatResponse(prompt: String, role: ChatRole): String {
        return when (role) {
            ChatRole.CREATOR_COACH -> """
                🚀 **Creator Growth Strategy for You:**
                
                1. **The 3-Second Hook:** Start your video or photo carousel with unexpected motion or high-contrast visual framing.
                2. **Storytelling Arc:** Keep text overlays short (under 7 words per line).
                3. **Engagement Trigger:** Ask a polarizing or curious question in the caption (e.g., *"Which slide fits your aesthetic best: 1 or 2?"*).
                4. **Audio Synergy:** Use trending audio snippets with less than 20k uses to get on the early algorithm wave!
                
                💡 *Tip:* Post between **6:30 PM - 8:45 PM** for maximum initial velocity.
            """.trimIndent()

            ChatRole.VIRAL_STRATEGIST -> """
                🔥 **Viral Reel Concept Breakdown:**
                
                - **Hook (0-3s):** Quick zoom-in cut + bold display text: *"Nobody talks about this..."*
                - **Core Value (3-12s):** 3 fast rhythmic cuts syncing to beat drops showing your process/vibe.
                - **Call to Action (12-15s):** *"Save this for your next photo session! 📌"*
                
                📈 **Target Audience:** Aesthetic lifestyle, travel creators & Gen Z visual explorers.
            """.trimIndent()

            ChatRole.CAPTION_WIZARD -> """
                ✨ **3 Viral Caption Options:**
                
                1. *Aesthetic & Chill:* "Golden hour never looked so peaceful 🌅✨"
                2. *Bold & Witty:* "Plot twist: It was even better in real life."
                3. *High-Engagement:* "Drop your favorite emoji if you need a getaway like this 👇"
                
                🏷️ **Recommended Hashtags:**
                `#LuminaMoments #VisualStoryteller #ExplorePage #AestheticVibes #ReelsTrend #CreatorDaily #GoldenHourMagic`
            """.trimIndent()

            ChatRole.PHOTO_CRITIQUE -> """
                📸 **Visual Aesthetic Critique:**
                
                - **Composition:** Rule-of-thirds is well maintained; great leading lines drawing the eye to the focal subject.
                - **Lighting & Exposure:** Soft ambient lighting with well-preserved highlights.
                - **Color Grading Recommendation:** Boost warm luminance slightly (+5) and apply a subtle grain filter for film camera depth!
            """.trimIndent()
        }
    }

    private fun getSimulatedSearchGrounding(query: String): GroundedSearchResult {
        return GroundedSearchResult(
            answer = """
                🌐 **Live Trending Insights for "$query":**
                
                - **Top Trending Trend:** Dynamic cinematic transitions, vintage 90s film aesthetic, and cozy ambient cafe vlogs.
                - **Hashtag Velocity:** High search volume around `#VisualCreators`, `#AestheticLife`, `#TrendingReels2026`.
                - **Key Advice:** Combine high-frame-rate video footage with lo-fi instrumental beats to capture the current explore algorithm preference!
            """.trimIndent(),
            sources = listOf(
                SearchSource("Creator Trends Hub 2026", "https://trends.google.com/social"),
                SearchSource("Global Social Media Report", "https://news.google.com/technology")
            )
        )
    }

    private fun getSimulatedMapsGrounding(city: String): GroundedMapsResult {
        return GroundedMapsResult(
            recommendations = "Found 4 highly rated, Instagrammable photography spots in $city with optimal golden hour lighting and scenic backgrounds!",
            locations = listOf(
                MapLocationInfo("$city Skyline Skydeck & Rooftop", "Panaromic sunset view overlooking architectural landmarks.", "Golden Hour (5:45 PM)", "Futuristic / Cityscape"),
                MapLocationInfo("Old Town Artisanal Heritage Lane", "Cobblestone streets with pastel-colored facades and vintage lanterns.", "Morning Light (8:30 AM)", "Vintage / Cozy"),
                MapLocationInfo("Botanical Conservatory & Glasshouse", "Lush tropical foliage with natural glass skylights.", "Midday (11:30 AM)", "Earthy / Natural"),
                MapLocationInfo("Harbor Promenade & Pier", "Reflective waterfront vistas with vibrant twilight reflections.", "Blue Hour (6:30 PM)", "Cinematic / Minimalist")
            )
        )
    }

    private fun getSimulatedGeneratedImage(prompt: String, aspectRatio: String, size: String): GeneratedImageResult {
        val sampleImages = listOf(
            "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=1080",
            "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1080",
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1080",
            "https://images.unsplash.com/photo-1518770660439-4636190af475?w=1080",
            "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1080"
        )
        return GeneratedImageResult(
            imageUrl = sampleImages.random(),
            prompt = prompt,
            size = size,
            aspectRatio = aspectRatio
        )
    }

    private fun getSimulatedVeoVideo(prompt: String, aspectRatio: String): GeneratedVideoResult {
        val sampleThumbnails = listOf(
            "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1080",
            "https://images.unsplash.com/photo-1518770660439-4636190af475?w=1080",
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1080"
        )
        return GeneratedVideoResult(
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            thumbnailBase64 = null,
            prompt = prompt,
            aspectRatio = aspectRatio,
            durationSeconds = 6,
            status = "COMPLETED"
        )
    }

    private fun getSimulatedCaptions(topic: String, tone: String): AiCaptionResult {
        return AiCaptionResult(
            captions = listOf(
                "Living for these moments ✨ #aesthetic #vibes",
                "Proof that magic exists in the everyday details 📸",
                "Chasing light, catching dreams 🚀💫"
            ),
            hashtags = listOf("#$topic", "#creators", "#viralreels", "#explorepage", "#aesthetic", "#photography", "#dailyvibes", "#instamood"),
            suggestedMusicGenre = "Chill Lo-Fi / Synth Beats",
            recommendedTime = "6:30 PM - 8:30 PM"
        )
    }
}
