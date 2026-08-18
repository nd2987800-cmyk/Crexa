package com.example.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.gemini.GeminiService
import com.example.ui.components.SimpleTopBar
import com.example.ui.components.SmartMediaImage
import com.example.ui.theme.CrexaPurple
import com.example.ui.theme.StoryGradient
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CreatePostScreen(
    onCreatePost: (mediaUrl: String, caption: String, location: String, hashtags: String, filter: String) -> Unit,
    onCreateReel: (videoUrl: String, caption: String, audioTitle: String) -> Unit,
    onCreateStory: (mediaUrl: String, caption: String) -> Unit,
    onBackClick: () -> Unit,
    onOpenFullCamera: () -> Unit = {},
    onOpenAiStudio: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val geminiService = remember { GeminiService() }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Post, 1 = Reel, 2 = Story, 3 = Live
    var selectedSource by remember { mutableStateOf(1) } // 0 = Camera, 1 = Gallery

    var selectedMediaUrl by remember {
        mutableStateOf("android.resource://com.aistudio.lumina.social/drawable/img_sample_post_1_1786177193097")
    }
    var isDeviceMediaSelected by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Normal") }

    var caption by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var hashtags by remember { mutableStateOf("") }
    var audioTitle by remember { mutableStateOf("") }
    var taggedUsers by remember { mutableStateOf("") }
    var isCloseFriendsOnly by remember { mutableStateOf(false) }
    var isRemixDuetEnabled by remember { mutableStateOf(true) }
    var isCollabPost by remember { mutableStateOf(false) }
    var collabCoAuthor by remember { mutableStateOf("") }
    var isWatermarkEnabled by remember { mutableStateOf(true) }
    var showMusicDialog by remember { mutableStateOf(false) }
    var isDualCameraActive by remember { mutableStateOf(false) }
    var isAiSubtitlesGenerating by remember { mutableStateOf(false) }
    var isAiVoiceoverActive by remember { mutableStateOf(false) }

    // Live Streaming State
    var isLiveActive by remember { mutableStateOf(false) }
    var liveViewerCount by remember { mutableStateOf(128) }
    var liveTopic by remember { mutableStateOf("Chilling with Crexa fam ✨") }

    // AI States
    var isAiGeneratingCaption by remember { mutableStateOf(false) }
    var isAiCritiquing by remember { mutableStateOf(false) }
    var critiqueScore by remember { mutableStateOf<Int?>(null) }
    var critiqueFeedback by remember { mutableStateOf<String?>(null) }
    var selectedAiTone by remember { mutableStateOf("Aesthetic") }

    // Storage permissions
    val storagePermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    val storagePermissionsState = rememberMultiplePermissionsState(permissions = storagePermissions)

    // Camera permissions
    val cameraPermissions = remember {
        listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    }
    val cameraPermissionsState = rememberMultiplePermissionsState(permissions = cameraPermissions)

    // System Photo/Video Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedMediaUrl = uri.toString()
            isDeviceMediaSelected = true
            Toast.makeText(context, "Loaded from Gallery!", Toast.LENGTH_SHORT).show()
        }
    }

    // Generic GetContent fallback launcher
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedMediaUrl = uri.toString()
            isDeviceMediaSelected = true
            Toast.makeText(context, "Loaded from Gallery!", Toast.LENGTH_SHORT).show()
        }
    }

    val launchGalleryPicker = {
        if (storagePermissionsState.allPermissionsGranted || Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(
                        if (selectedTab == 1) ActivityResultContracts.PickVisualMedia.VideoOnly
                        else ActivityResultContracts.PickVisualMedia.ImageAndVideo
                    )
                )
            } catch (e: Exception) {
                getContentLauncher.launch(if (selectedTab == 1) "video/*" else "image/*")
            }
        } else {
            storagePermissionsState.launchMultiplePermissionRequest()
        }
    }

    val sampleGalleryImages = listOf(
        "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_1_1786177193097",
        "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_2_1786177203745",
        "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_3_1786177217178",
        "android.resource://com.aistudio.lumina.social/drawable/img_crexa_brand_logo_1786179516858"
    )

    val filters = listOf("Normal", "Vivid", "Warm", "Mono", "Cyber", "Golden")

    Scaffold(
        containerColor = Color.White,
        topBar = {
            SimpleTopBar(
                title = when (selectedTab) {
                    0 -> "New Post"
                    1 -> "New Reel"
                    2 -> "New Story"
                    else -> "Go Live"
                },
                onBackClick = onBackClick,
                actions = {
                    if (selectedTab != 3) {
                        Button(
                            onClick = {
                                when (selectedTab) {
                                    0 -> onCreatePost(selectedMediaUrl, caption, location, hashtags, selectedFilter)
                                    1 -> onCreateReel(selectedMediaUrl, caption, if (audioTitle.isBlank()) "Original Sound" else audioTitle)
                                    2 -> onCreateStory(selectedMediaUrl, caption)
                                }
                                Toast.makeText(context, "Published successfully to Crexa!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrexaPurple),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .testTag("btn_publish_create")
                        ) {
                            Text("Share", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Target Format Switcher (Post, Reel, Story, Live)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = CrexaPurple
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Post", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Reel", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Story", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Live", fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
            }

            if (selectedTab == 3) {
                // LIVE STREAM BROADCAST VIEW
                LiveBroadcastSetupCard(
                    topic = liveTopic,
                    onTopicChange = { liveTopic = it },
                    isLiveActive = isLiveActive,
                    viewerCount = liveViewerCount,
                    onToggleLive = {
                        if (!isLiveActive) {
                            if (cameraPermissionsState.allPermissionsGranted) {
                                isLiveActive = true
                                Toast.makeText(context, "You are now LIVE on Crexa! 🔴", Toast.LENGTH_LONG).show()
                            } else {
                                cameraPermissionsState.launchMultiplePermissionRequest()
                            }
                        } else {
                            isLiveActive = false
                            Toast.makeText(context, "Live stream ended. Saved replay!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onOpenFullCameraLive = onOpenFullCamera
                )
            } else {
                // Camera vs Gallery Source Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    FilterChip(
                        selected = selectedSource == 1,
                        onClick = {
                            selectedSource = 1
                            launchGalleryPicker()
                        },
                        label = { Text("Upload from Gallery 📁", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFF5F3FF),
                            selectedLabelColor = CrexaPurple
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedSource == 1,
                            borderColor = Color(0xFFE2E8F0),
                            selectedBorderColor = CrexaPurple
                        )
                    )

                    FilterChip(
                        selected = selectedSource == 0,
                        onClick = {
                            selectedSource = 0
                            if (cameraPermissionsState.allPermissionsGranted) {
                                onOpenFullCamera()
                            } else {
                                cameraPermissionsState.launchMultiplePermissionRequest()
                            }
                        },
                        label = { Text("Direct Camera 📷", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFF5F3FF),
                            selectedLabelColor = CrexaPurple
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedSource == 0,
                            borderColor = Color(0xFFE2E8F0),
                            selectedBorderColor = CrexaPurple
                        )
                    )
                }

                // Media Preview Frame
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(if (selectedTab == 1) 320.dp else 260.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F172A))
                    ) {
                        SmartMediaImage(
                            mediaUrl = selectedMediaUrl,
                            contentDescription = "Selected Media Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (selectedTab == 1) {
                            // Reel play badge overlay
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                            }

                            // Dual Camera Overlay Picture-in-Picture
                            if (isDualCameraActive) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(2.dp, CrexaPurple),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .size(width = 80.dp, height = 110.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF1E293B)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Face, contentDescription = "Front Cam", tint = Color.White, modifier = Modifier.size(24.dp))
                                            Text("Front Cam", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            Text("Dual 🔄", fontSize = 8.sp, color = CrexaPurple)
                                        }
                                    }
                                }
                            }

                            // Dual Camera Mode Toggle
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isDualCameraActive) CrexaPurple else Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                                    .clickable {
                                        isDualCameraActive = !isDualCameraActive
                                        Toast.makeText(context, if (isDualCameraActive) "Dual Camera Mode ON (Front + Back)" else "Dual Camera Mode OFF", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Cameraswitch, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isDualCameraActive) "Dual ON" else "Dual Cam",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Top-right Change/Picker Button Overlay
                        Button(
                            onClick = launchGalleryPicker,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change Media", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Direct Gallery Chooser Bar
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gallery Photos & Videos",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        )
                        TextButton(onClick = launchGalleryPicker) {
                            Text("Open Device Gallery ↗", color = CrexaPurple, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Open Device Picker Action Card
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = BorderStroke(1.5.dp, CrexaPurple),
                                modifier = Modifier
                                    .size(76.dp)
                                    .clickable { launchGalleryPicker() }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Pick file",
                                        tint = CrexaPurple,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Pick File", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CrexaPurple)
                                }
                            }
                        }

                        // Presets
                        items(sampleGalleryImages) { media ->
                            val isSelected = selectedMediaUrl == media
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) CrexaPurple else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        selectedMediaUrl = media
                                        isDeviceMediaSelected = false
                                    }
                            ) {
                                SmartMediaImage(
                                    mediaUrl = media,
                                    contentDescription = "Sample thumbnail",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(CrexaPurple),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Direct Camera Quick Launch Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable {
                            if (cameraPermissionsState.allPermissionsGranted) {
                                onOpenFullCamera()
                            } else {
                                cameraPermissionsState.launchMultiplePermissionRequest()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = CrexaPurple,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Capture Directly with Camera", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                Text("Record Video Reel, Snap Post, or Story", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    }
                }

                // Filter Selector & AI Magic Enhance
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Color Filters & AI Magic",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    )
                    SuggestionChip(
                        onClick = {
                            selectedFilter = if (selectedFilter == "AI Magic ✨") "Normal" else "AI Magic ✨"
                            Toast.makeText(context, "🪄 Gemini AI Magic Enhancement Applied! Colors optimized.", Toast.LENGTH_SHORT).show()
                        },
                        label = { Text("🪄 1-Tap AI Enhance", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CrexaPurple) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFF5F3FF)),
                        border = BorderStroke(1.dp, CrexaPurple)
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val allFilters = listOf("Normal", "AI Magic ✨", "Vivid", "Warm", "Mono", "Cyber", "Golden")
                    items(allFilters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = {
                                selectedFilter = filter
                                if (filter == "AI Magic ✨") {
                                    Toast.makeText(context, "🪄 Gemini AI Auto Enhancing Contrast & Dynamic Range!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFF5F3FF),
                                selectedLabelColor = CrexaPurple
                            )
                        )
                    }
                }

                // Input fields
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        label = { Text("Write a caption...") },
                        placeholder = { Text("What's on your mind? #trending") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrexaPurple,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_create_caption")
                    )

                    if (selectedTab == 0 || selectedTab == 2) {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Add Location (e.g. Mumbai, New York, Tokyo)") },
                            leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = CrexaPurple) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrexaPurple,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = hashtags,
                            onValueChange = { hashtags = it },
                            label = { Text("Hashtags (comma separated: travel, art, viral)") },
                            leadingIcon = { Icon(Icons.Outlined.Tag, contentDescription = null, tint = CrexaPurple) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrexaPurple,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (selectedTab == 1) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = audioTitle,
                                onValueChange = { audioTitle = it },
                                label = { Text("Audio Track / Song Name") },
                                placeholder = { Text("e.g. Crexa Vibes - Original Mix") },
                                leadingIcon = { Icon(Icons.Outlined.MusicNote, contentDescription = null, tint = CrexaPurple) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrexaPurple,
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    val popularTracks = listOf("Cyberpunk Skyline ✨", "Midnight Chill Beats 🎧", "Lofi Sunset Glow 🌅", "Crexa Anthem ⚡")
                                    audioTitle = popularTracks.random()
                                    Toast.makeText(context, "Added track: $audioTitle", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = CrexaPurple.copy(alpha = 0.15f), contentColor = CrexaPurple),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Icon(Icons.Default.LibraryMusic, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Music", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        // AI Auto-Subtitles & Voiceover Card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                            border = BorderStroke(1.dp, CrexaPurple.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Subtitles, contentDescription = null, tint = CrexaPurple, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("AI Auto-Captions & Subtitles", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CrexaPurple)
                                    }
                                    Button(
                                        onClick = {
                                            if (!isAiSubtitlesGenerating) {
                                                isAiSubtitlesGenerating = true
                                                scope.launch {
                                                    delay(1200)
                                                    isAiSubtitlesGenerating = false
                                                    caption = if (caption.isBlank()) "[0:00] Hey Crexa fam! ✨\n[0:03] Today exploring hidden spots 🚀\n[0:08] Drop a like & follow for more!"
                                                    else "$caption\n\n📝 [AI Auto-Subtitles Generated]"
                                                    Toast.makeText(context, "AI Subtitles synced with audio!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CrexaPurple),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        if (isAiSubtitlesGenerating) {
                                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White)
                                        } else {
                                            Text("Generate", fontSize = 11.sp)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color(0xFF6D28D9), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("AI Voiceover Narration", fontSize = 12.sp, color = Color(0xFF1E1B4B))
                                    }
                                    Switch(
                                        checked = isAiVoiceoverActive,
                                        onCheckedChange = {
                                            isAiVoiceoverActive = it
                                            Toast.makeText(context, if (it) "🎙️ Gemini AI Voiceover enabled for this Reel" else "Voiceover disabled", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }

                        // Remix & Duet Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Allow Remix & Duets", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Let other creators react or collab with your reel", fontSize = 11.sp, color = Color.Gray)
                            }
                            Switch(
                                checked = isRemixDuetEnabled,
                                onCheckedChange = { isRemixDuetEnabled = it }
                            )
                        }
                    }

                    // Tag People & Collaborators
                    OutlinedTextField(
                        value = taggedUsers,
                        onValueChange = { taggedUsers = it },
                        label = { Text("Tag People / Collaborator (e.g. @alex, @sarah)") },
                        leadingIcon = { Icon(Icons.Outlined.PersonAddAlt, contentDescription = null, tint = CrexaPurple) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrexaPurple,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Close Friends Only switch (for Stories & Posts)
                    if (selectedTab == 0 || selectedTab == 2) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isCloseFriendsOnly) Color(0xFFECFDF5) else Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, if (isCloseFriendsOnly) Color(0xFF10B981) else Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (isCloseFriendsOnly) Color(0xFF10B981) else Color.Gray,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Close Friends Only 🟢", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isCloseFriendsOnly) Color(0xFF065F46) else Color.Black)
                                        Text("Only your custom close friends circle can see this", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                                Switch(
                                    checked = isCloseFriendsOnly,
                                    onCheckedChange = {
                                        isCloseFriendsOnly = it
                                        Toast.makeText(context, if (it) "Audience set to Close Friends 🟢" else "Audience set to Public", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    // Collab Post / Invite Co-Author Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isCollabPost) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, if (isCollabPost) Color(0xFF86EFAC) else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.GroupAdd,
                                        contentDescription = null,
                                        tint = if (isCollabPost) Color(0xFF10B981) else CrexaPurple,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Invite Co-Author / Collab Post 🤝", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isCollabPost) Color(0xFF065F46) else Color.Black)
                                        Text("Shares view count, likes, and reach on both profiles", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                                Switch(
                                    checked = isCollabPost,
                                    onCheckedChange = { isCollabPost = it }
                                )
                            }

                            if (isCollabPost) {
                                OutlinedTextField(
                                    value = collabCoAuthor,
                                    onValueChange = { collabCoAuthor = it },
                                    label = { Text("Co-Author Username (e.g. @priya_creatives)") },
                                    placeholder = { Text("@username") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Auto Watermark Toggle
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BrandingWatermark,
                                    contentDescription = null,
                                    tint = CrexaPurple,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Add Crexa & @Username Watermark", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Protects your original content when saved or shared externally", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            Switch(
                                checked = isWatermarkEnabled,
                                onCheckedChange = { isWatermarkEnabled = it }
                            )
                        }
                    }

                    // --- GEMINI AI ASSIST CARD ---
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F3FF)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFDDD6FE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CrexaPurple)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Crexa AI Assist", fontWeight = FontWeight.Bold, color = CrexaPurple)
                                }
                                TextButton(onClick = onOpenAiStudio) {
                                    Text("Full AI Studio →", fontSize = 12.sp, color = CrexaPurple)
                                }
                            }

                            Text("Select AI Tone Style:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(listOf("Aesthetic", "Viral Hype", "Witty", "Minimalist", "Poetic")) { tone ->
                                    FilterChip(
                                        selected = selectedAiTone == tone,
                                        onClick = { selectedAiTone = tone },
                                        label = { Text(tone, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (!isAiGeneratingCaption) {
                                            isAiGeneratingCaption = true
                                            scope.launch {
                                                val result = geminiService.generateCaptionsAndHashtags(
                                                    topicOrVibe = if (caption.isNotBlank()) caption else "Lifestyle aesthetic vibe",
                                                    tone = selectedAiTone
                                                )
                                                isAiGeneratingCaption = false
                                                result.getOrNull()?.let { aiRes ->
                                                    caption = aiRes.captions.firstOrNull() ?: caption
                                                    hashtags = aiRes.hashtags.joinToString(", ")
                                                    if (audioTitle.isBlank()) {
                                                        audioTitle = aiRes.suggestedMusicGenre
                                                    }
                                                    Toast.makeText(context, "AI Generated Captions & Tags!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isAiGeneratingCaption,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CrexaPurple)
                                ) {
                                    if (isAiGeneratingCaption) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Generating...")
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("AI Captions")
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (!isAiCritiquing) {
                                            isAiCritiquing = true
                                            scope.launch {
                                                val res = geminiService.critiquePostViralScore(caption, hashtags)
                                                isAiCritiquing = false
                                                res.getOrNull()?.let { critique ->
                                                    critiqueScore = critique.score
                                                    critiqueFeedback = "${critique.verdict}\n• " + (critique.strengths.firstOrNull() ?: "High engagement")
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isAiCritiquing,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isAiCritiquing) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                    } else {
                                        Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Viral Score")
                                    }
                                }
                            }

                            critiqueScore?.let { score ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = CrexaPurple,
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("$score", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Viral Score: $score / 100", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                            Text(critiqueFeedback ?: "", fontSize = 11.sp, color = Color(0xFF64748B))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Direct bottom submit button
                    Button(
                        onClick = {
                            when (selectedTab) {
                                0 -> onCreatePost(selectedMediaUrl, caption, location, hashtags, selectedFilter)
                                1 -> onCreateReel(selectedMediaUrl, caption, if (audioTitle.isBlank()) "Original Sound" else audioTitle)
                                2 -> onCreateStory(selectedMediaUrl, caption)
                            }
                            Toast.makeText(context, "Published successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrexaPurple),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = when (selectedTab) {
                                0 -> "Share Post Now"
                                1 -> "Upload Video Reel Now"
                                else -> "Share to Story Now"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveBroadcastSetupCard(
    topic: String,
    onTopicChange: (String) -> Unit,
    isLiveActive: Boolean,
    viewerCount: Int,
    onToggleLive: () -> Unit,
    onOpenFullCameraLive: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isLiveActive) Color(0xFF0F172A) else Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, if (isLiveActive) Color.Red else Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (isLiveActive) {
                // Active Live Stream View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.Red,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("LIVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$viewerCount watching", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    SmartMediaImage(
                        mediaUrl = "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_1_1786177193097",
                        contentDescription = "Live feed",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Live comments simulation overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text("🔥 @alex_creator: loving the stream!", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("❤️ @zara_art: hello from Tokyo!", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }

                Button(
                    onClick = onToggleLive,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("End Live Stream", fontWeight = FontWeight.Bold)
                }
            } else {
                // Setup Broadcast View
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = "Go Live to Your Crexa Followers",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                )

                Text(
                    text = "Broadcast live video, interact with real-time comments, and share your moments directly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = onTopicChange,
                    label = { Text("Live Broadcast Title") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onToggleLive,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Podcasts, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Live", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onOpenFullCameraLive,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Camera")
                    }
                }
            }
        }
    }
}
