package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.SimpleTopBar

@Composable
fun CreatePostScreen(
    onCreatePost: (mediaUrl: String, caption: String, location: String, hashtags: String, filter: String) -> Unit,
    onCreateReel: (videoUrl: String, caption: String, audioTitle: String) -> Unit,
    onCreateStory: (mediaUrl: String, caption: String) -> Unit,
    onBackClick: () -> Unit,
    onOpenFullCamera: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Post, 1 = Reel, 2 = Story
    var selectedSource by remember { mutableStateOf(0) } // 0 = Camera, 1 = Gallery

    var selectedMediaUrl by remember { mutableStateOf("android.resource://com.aistudio.lumina.social/drawable/img_sample_post_1_1786177193097") }
    var selectedFilter by remember { mutableStateOf("Normal") }

    var caption by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var hashtags by remember { mutableStateOf("") }
    var audioTitle by remember { mutableStateOf("") }

    val sampleGalleryImages = listOf(
        "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_1_1786177193097",
        "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_2_1786177203745",
        "android.resource://com.aistudio.lumina.social/drawable/img_sample_post_3_1786177217178",
        "android.resource://com.aistudio.lumina.social/drawable/img_lumina_logo_1786177176474"
    )

    val filters = listOf("Normal", "Vivid", "Warm", "Mono", "Cyber")

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = when (selectedTab) {
                    0 -> "New Post"
                    1 -> "New Reel"
                    else -> "New Story"
                },
                onBackClick = onBackClick,
                actions = {
                    Button(
                        onClick = {
                            when (selectedTab) {
                                0 -> onCreatePost(selectedMediaUrl, caption, location, hashtags, selectedFilter)
                                1 -> onCreateReel(selectedMediaUrl, caption, audioTitle)
                                2 -> onCreateStory(selectedMediaUrl, caption)
                            }
                            Toast.makeText(context, "Published successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("btn_publish_create")
                    ) {
                        Text("Share", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Target Format Switcher
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Post") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Reel") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Story") })
            }

            // Camera vs Gallery Source toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                FilterChip(
                    selected = selectedSource == 0,
                    onClick = { selectedSource = 0 },
                    label = { Text("Camera") },
                    leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                FilterChip(
                    selected = selectedSource == 1,
                    onClick = { selectedSource = 1 },
                    label = { Text("Gallery") },
                    leadingIcon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Preview Box
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (selectedTab == 1) 320.dp else 260.dp)
                    .background(Color.Black)
            ) {
                if (selectedSource == 0) {
                    // Simulated Live Camera Viewfinder
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val previewImageId = remember(selectedMediaUrl) {
                            when {
                                selectedMediaUrl.contains("img_sample_post_1") -> R.drawable.img_sample_post_1_1786177193097
                                selectedMediaUrl.contains("img_sample_post_2") -> R.drawable.img_sample_post_2_1786177203745
                                selectedMediaUrl.contains("img_sample_post_3") -> R.drawable.img_sample_post_3_1786177217178
                                else -> R.drawable.img_crexa_brand_logo_1786179516858
                            }
                        }

                        Image(
                            painter = painterResource(id = previewImageId),
                            contentDescription = "Camera Viewfinder",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Camera controls overlay
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        ) {
                            Button(
                                onClick = onOpenFullCamera,
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("btn_open_camerax")
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open CameraX", fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Captured photo!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .testTag("btn_quick_snap")
                            ) {
                                Icon(Icons.Default.Camera, contentDescription = "Snap", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                } else {
                    val previewImageId = remember(selectedMediaUrl) {
                        when {
                            selectedMediaUrl.contains("img_sample_post_1") -> R.drawable.img_sample_post_1_1786177193097
                            selectedMediaUrl.contains("img_sample_post_2") -> R.drawable.img_sample_post_2_1786177203745
                            selectedMediaUrl.contains("img_sample_post_3") -> R.drawable.img_sample_post_3_1786177217178
                            else -> R.drawable.img_lumina_logo_1786177176474
                        }
                    }

                    Image(
                        painter = painterResource(id = previewImageId),
                        contentDescription = "Selected Media",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Gallery selector grid if Gallery tab active
            if (selectedSource == 1) {
                Text(
                    text = "Select from Gallery",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sampleGalleryImages) { media ->
                        val resId = when {
                            media.contains("img_sample_post_1") -> R.drawable.img_sample_post_1_1786177193097
                            media.contains("img_sample_post_2") -> R.drawable.img_sample_post_2_1786177203745
                            media.contains("img_sample_post_3") -> R.drawable.img_sample_post_3_1786177217178
                            else -> R.drawable.img_lumina_logo_1786177176474
                        }

                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = "Gallery thumb",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (selectedMediaUrl == media) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedMediaUrl = media }
                        )
                    }
                }
            }

            // Filter Selector
            Text(
                text = "Color Filter",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_create_caption")
                )

                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Add Location (e.g. Shinjuku, Tokyo)") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = hashtags,
                        onValueChange = { hashtags = it },
                        label = { Text("Hashtags (comma separated)") },
                        leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (selectedTab == 1) {
                    OutlinedTextField(
                        value = audioTitle,
                        onValueChange = { audioTitle = it },
                        label = { Text("Audio Track Name") },
                        leadingIcon = { Icon(Icons.Default.MusicNote, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
