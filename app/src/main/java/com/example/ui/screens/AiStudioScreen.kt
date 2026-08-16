package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.gemini.*
import kotlinx.coroutines.launch

enum class AiStudioTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CHAT("AI Chatbot", Icons.Filled.SmartToy),
    IMAGE_GEN("Image Studio", Icons.Filled.Image),
    VEO_ANIMATE("Veo Video", Icons.Filled.MovieCreation),
    GROUNDING("Maps & Search", Icons.Filled.Public)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStudioScreen(
    onBackClick: () -> Unit,
    onCreatePostFromMedia: (mediaUrl: String, caption: String, hashtags: String) -> Unit,
    onCreateReelFromMedia: (videoUrl: String, caption: String) -> Unit,
    initialTab: AiStudioTab = AiStudioTab.CHAT
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val scope = rememberCoroutineScope()
    val geminiService = remember { GeminiService() }

    // Chatbot State
    var selectedRole by remember { mutableStateOf(ChatRole.CREATOR_COACH) }
    var selectedModel by remember { mutableStateOf("gemini-3.5-flash") }
    var chatMessages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    text = "👋 Hello creator! I am your AI Creator Assistant powered by Gemini. Ask me for viral reel scripts, caption ideas, aesthetic photo critiques, or growth strategies!",
                    isUser = false
                )
            )
        )
    }
    var chatInput by remember { mutableStateOf("") }
    var isChatLoading by remember { mutableStateOf(false) }

    // Image Studio State
    var imagePrompt by remember { mutableStateOf("Aesthetic sunset coffee table with vintage film camera and warm golden sunlight") }
    var selectedResolution by remember { mutableStateOf("2K") } // 1K, 2K, 4K
    var selectedAspectRatio by remember { mutableStateOf("1:1") } // 1:1, 9:16, 16:9, 4:5
    var isImageGenerating by remember { mutableStateOf(false) }
    var generatedImageResult by remember { mutableStateOf<GeneratedImageResult?>(null) }

    // Veo Video Animator State
    var veoPrompt by remember { mutableStateOf("Cinematic gentle zoom into glowing neon lights, floating dust particles, smooth 60fps motion") }
    var selectedVeoAspect by remember { mutableStateOf("9:16") } // 9:16 or 16:9
    var isVeoGenerating by remember { mutableStateOf(false) }
    var generatedVideoResult by remember { mutableStateOf<GeneratedVideoResult?>(null) }
    var selectedInputImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedInputImageUri = uri
    }

    // Grounding (Maps & Search) State
    var searchGroundingQuery by remember { mutableStateOf("trending fashion and aesthetic reels trends this week") }
    var searchResult by remember { mutableStateOf<GroundedSearchResult?>(null) }
    var isSearchLoading by remember { mutableStateOf(false) }

    var mapsCityQuery by remember { mutableStateOf("Mumbai") }
    var mapsResult by remember { mutableStateOf<GroundedMapsResult?>(null) }
    var isMapsLoading by remember { mutableStateOf(false) }

    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(gradientColors)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "AI Studio",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Crexa AI Studio",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Crexa Vision & Intelligence Suite",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("ai_studio_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                AiStudioTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(tab.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(tab.label, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal)
                            }
                        },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }

            when (selectedTab) {
                // 1. GEMINI CHATBOT (Multi-turn, Roles, Model selector)
                AiStudioTab.CHAT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                    ) {
                        // Role Selection Chips
                        LazyRow(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(ChatRole.values().toList()) { role ->
                                FilterChip(
                                    selected = selectedRole == role,
                                    onClick = {
                                        selectedRole = role
                                        selectedModel = role.defaultModel
                                    },
                                    label = { Text(role.displayName, fontSize = 12.sp) },
                                    leadingIcon = {
                                        if (selectedRole == role) {
                                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    },
                                    modifier = Modifier.testTag("role_chip_${role.name.lowercase()}")
                                )
                            }
                        }

                        // Model Badge & Quick Prompt Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = "⚡ $selectedModel",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = "Multi-turn Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Message Thread
                        val listState = rememberLazyListState()
                        LaunchedEffect(chatMessages.size) {
                            if (chatMessages.isNotEmpty()) {
                                listState.animateScrollToItem(chatMessages.size - 1)
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(chatMessages) { message ->
                                ChatBubbleItem(message = message)
                            }
                            if (isChatLoading) {
                                item {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            "Gemini is thinking...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        // Quick Prompt Suggestions
                        LazyRow(
                            modifier = Modifier.padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val suggestions = listOf(
                                "🔥 3 Viral Reel Hook ideas",
                                "✨ Aesthetic caption for sunset photo",
                                "📈 How to get on Explore Page in 2026",
                                "📸 Photo critique & lighting tips"
                            )
                            items(suggestions) { sugg ->
                                SuggestionChip(
                                    onClick = {
                                        chatInput = sugg
                                    },
                                    label = { Text(sugg, fontSize = 11.sp) }
                                )
                            }
                        }

                        // Input Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = { Text("Ask Gemini anything for your content...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_input_field"),
                                maxLines = 3,
                                shape = RoundedCornerShape(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    val text = chatInput.trim()
                                    if (text.isNotBlank() && !isChatLoading) {
                                        val userMsg = ChatMessage(text = text, isUser = true)
                                        chatMessages = chatMessages + userMsg
                                        chatInput = ""
                                        isChatLoading = true

                                        scope.launch {
                                            val response = geminiService.chatWithGemini(
                                                prompt = text,
                                                history = chatMessages,
                                                role = selectedRole,
                                                selectedModel = selectedModel
                                            )
                                            isChatLoading = false
                                            val aiText = response.getOrNull() ?: "I am here to help you create amazing content!"
                                            chatMessages = chatMessages + ChatMessage(text = aiText, isUser = false)
                                        }
                                    }
                                },
                                enabled = chatInput.isNotBlank() && !isChatLoading,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (chatInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("chat_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (chatInput.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 2. HIGH-QUALITY IMAGE GENERATION (gemini-3-pro-image-preview, 1K, 2K, 4K)
                AiStudioTab.IMAGE_GEN -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "🎨 High-Quality AI Image Generator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Powered by gemini-3-pro-image-preview with 1K/2K/4K ultra resolution.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = imagePrompt,
                            onValueChange = { imagePrompt = it },
                            label = { Text("Prompt description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("image_prompt_field"),
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Style presets
                        Text("Style Presets", style = MaterialTheme.typography.labelMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val styles = listOf("Photorealistic 35mm", "Cyberpunk Neon", "Cinematic Film", "Pastel Minimalist", "3D Blender Art", "Vintage Polaroid")
                            items(styles) { st ->
                                AssistChip(
                                    onClick = { imagePrompt = "$imagePrompt, in $st style, ultra detailed lighting" },
                                    label = { Text(st, fontSize = 11.sp) }
                                )
                            }
                        }

                        // Aspect Ratio Selector
                        Text("Aspect Ratio", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("1:1", "9:16", "16:9", "4:5").forEach { aspect ->
                                FilterChip(
                                    selected = selectedAspectRatio == aspect,
                                    onClick = { selectedAspectRatio = aspect },
                                    label = { Text(aspect) },
                                    modifier = Modifier.testTag("aspect_chip_$aspect")
                                )
                            }
                        }

                        // Resolution Selector (1K, 2K, 4K)
                        Text("Image Resolution (Affordance)", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("1K", "2K", "4K").forEach { res ->
                                FilterChip(
                                    selected = selectedResolution == res,
                                    onClick = { selectedResolution = res },
                                    label = { Text("$res Resolution") },
                                    leadingIcon = {
                                        if (selectedResolution == res) {
                                            Icon(Icons.Filled.HighQuality, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    },
                                    modifier = Modifier.testTag("res_chip_$res")
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (imagePrompt.isNotBlank() && !isImageGenerating) {
                                    isImageGenerating = true
                                    scope.launch {
                                        val result = geminiService.generateHighQualityImage(
                                            prompt = imagePrompt,
                                            aspectRatio = selectedAspectRatio,
                                            imageSize = selectedResolution,
                                            model = "gemini-3-pro-image-preview"
                                        )
                                        isImageGenerating = false
                                        generatedImageResult = result.getOrNull()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("generate_image_button"),
                            enabled = imagePrompt.isNotBlank() && !isImageGenerating,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isImageGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generating $selectedResolution Artwork...")
                            } else {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate $selectedResolution Image")
                            }
                        }

                        // Generated Result View
                        generatedImageResult?.let { result ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("✨ Result (${result.size} • ${result.aspectRatio})", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    AsyncImage(
                                        model = result.imageUrl ?: "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=1080",
                                        contentDescription = result.prompt,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(280.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(result.prompt, style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val url = result.imageUrl ?: "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=1080"
                                                onCreatePostFromMedia(url, result.prompt, "#gemini #aiart #${result.size}")
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Post to Feed")
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val url = result.imageUrl ?: "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=1080"
                                                onCreateReelFromMedia(url, result.prompt)
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Filled.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("To Reel")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. VEO VIDEO ANIMATION (veo-3.1-fast-generate-preview, 16:9 or 9:16)
                AiStudioTab.VEO_ANIMATE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "🎬 Animate Images into Video with Veo",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Upload a photo and generate dynamic motion video with model veo-3.1-fast-generate-preview.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Upload photo box
                        Card(
                            onClick = { photoPicker.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                if (selectedInputImageUri != null) {
                                    AsyncImage(
                                        model = selectedInputImageUri,
                                        contentDescription = "Selected Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Tap to Select Photo to Animate", fontWeight = FontWeight.SemiBold)
                                        Text("JPG / PNG image format", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = veoPrompt,
                            onValueChange = { veoPrompt = it },
                            label = { Text("Motion & Animation Prompt") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Aspect Ratio (16:9 or 9:16)
                        Text("Veo Video Aspect Ratio (16:9 or 9:16)", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("9:16 (Portrait Reel)", "16:9 (Landscape)").forEach { item ->
                                val code = if (item.startsWith("9:16")) "9:16" else "16:9"
                                FilterChip(
                                    selected = selectedVeoAspect == code,
                                    onClick = { selectedVeoAspect = code },
                                    label = { Text(item) }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (veoPrompt.isNotBlank() && !isVeoGenerating) {
                                    isVeoGenerating = true
                                    scope.launch {
                                        val result = geminiService.generateVideoWithVeo(
                                            prompt = veoPrompt,
                                            aspectRatio = selectedVeoAspect,
                                            model = "veo-3.1-fast-generate-preview"
                                        )
                                        isVeoGenerating = false
                                        generatedVideoResult = result.getOrNull()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("generate_veo_button"),
                            enabled = veoPrompt.isNotBlank() && !isVeoGenerating,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isVeoGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Veo Generating Video Motion...")
                            } else {
                                Icon(Icons.Filled.VideoCameraBack, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Video with Veo")
                            }
                        }

                        generatedVideoResult?.let { video ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🎬 Veo Video Preview (${video.aspectRatio})", fontWeight = FontWeight.Bold)
                                        Badge { Text("READY") }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(240.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1080",
                                            contentDescription = "Video Thumbnail",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.Black.copy(alpha = 0.6f),
                                            modifier = Modifier.size(56.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.PlayArrow,
                                                contentDescription = "Play",
                                                tint = Color.White,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(video.prompt, style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            onCreateReelFromMedia(
                                                video.videoUrl ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                                                "Animated with Veo 3.1: ${video.prompt}"
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Filled.Publish, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Publish Directly as Reel")
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. MAPS & SEARCH GROUNDING
                AiStudioTab.GROUNDING -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Crexa Search Grounding Section
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.TravelExplore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Crexa Search Grounding", fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    "Powered by Crexa AI engine with real-time web grounding for live trends and citations.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = searchGroundingQuery,
                                    onValueChange = { searchGroundingQuery = it },
                                    label = { Text("Search trending social topic") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Button(
                                    onClick = {
                                        if (searchGroundingQuery.isNotBlank() && !isSearchLoading) {
                                            isSearchLoading = true
                                            scope.launch {
                                                val res = geminiService.searchWithGoogleGrounding(searchGroundingQuery)
                                                isSearchLoading = false
                                                searchResult = res.getOrNull()
                                            }
                                        }
                                    },
                                    enabled = searchGroundingQuery.isNotBlank() && !isSearchLoading,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isSearchLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Crexa Searching Live Web...")
                                    } else {
                                        Icon(Icons.Filled.Search, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Get Crexa Grounded Insights")
                                    }
                                }

                                searchResult?.let { res ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(res.answer, style = MaterialTheme.typography.bodySmall)
                                            if (res.sources.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("🔗 Web Sources:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                res.sources.forEach { s ->
                                                    Text("• ${s.title}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Crexa Maps Grounding Section
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Crexa Maps Grounding", fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    "Powered by Crexa Location AI for aesthetic photography spots, viral reels spots, and locations.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = mapsCityQuery,
                                    onValueChange = { mapsCityQuery = it },
                                    label = { Text("City / Destination (e.g. Paris, Goa, Tokyo, NYC)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Button(
                                    onClick = {
                                        if (mapsCityQuery.isNotBlank() && !isMapsLoading) {
                                            isMapsLoading = true
                                            scope.launch {
                                                val res = geminiService.exploreLocationsWithGoogleMaps(mapsCityQuery)
                                                isMapsLoading = false
                                                mapsResult = res.getOrNull()
                                            }
                                        }
                                    },
                                    enabled = mapsCityQuery.isNotBlank() && !isMapsLoading,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                ) {
                                    if (isMapsLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Crexa Exploring Maps...")
                                    } else {
                                        Icon(Icons.Filled.LocationSearching, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Find Photo & Shoot Spots")
                                    }
                                }

                                mapsResult?.let { res ->
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(res.recommendations, style = MaterialTheme.typography.bodySmall)
                                        res.locations.forEach { loc ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text(loc.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text(loc.description, fontSize = 12.sp)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row {
                                                        Text("⏰ ${loc.bestTimeToShoot}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text("🎨 ${loc.aestheticVibe}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(message: ChatMessage) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bg = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textCol = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            if (!isUser) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(top = 2.dp)
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = "AI",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(6.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = bg,
                tonalElevation = 2.dp
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textCol,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}
