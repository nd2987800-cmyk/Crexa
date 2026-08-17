package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.components.SmartMediaImage
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CrexaPurple
import com.example.ui.theme.DarkOverlayGradient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryViewerScreen(
    stories: List<StoryEntity>,
    users: List<UserEntity>,
    initialIndex: Int,
    currentUser: UserEntity? = null,
    onClose: () -> Unit,
    onSendReply: (recipientUserId: String, message: String) -> Unit,
    onFetchStoryViewers: suspend (storyId: String) -> List<UserEntity> = { emptyList() },
    onUserClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentIndex by remember { mutableStateOf(initialIndex) }
    var isPaused by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var showViewersSheet by remember { mutableStateOf(false) }
    var viewersList by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var isLoadingViewers by remember { mutableStateOf(false) }

    val userMap = remember(users) { users.associateBy { it.id } }

    if (stories.isEmpty() || currentIndex >= stories.size) {
        LaunchedEffect(Unit) { onClose() }
        return
    }

    val currentStory = stories[currentIndex]
    val author = userMap[currentStory.userId]
    val isMyStory = (currentUser != null && currentStory.userId == currentUser.id)

    val progress = remember { Animatable(0f) }

    // Load story viewers if it is my story
    LaunchedEffect(currentStory.id, isMyStory) {
        if (isMyStory) {
            isLoadingViewers = true
            viewersList = onFetchStoryViewers(currentStory.id)
            isLoadingViewers = false
        } else {
            viewersList = emptyList()
        }
    }

    LaunchedEffect(currentIndex, isPaused, showViewersSheet) {
        if (!isPaused && !showViewersSheet) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = ((1f - progress.value) * 5000).toInt(),
                    easing = LinearEasing
                )
            )
            if (currentIndex < stories.size - 1) {
                currentIndex++
                progress.snapTo(0f)
            } else {
                onClose()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(currentIndex, showViewersSheet) {
                if (!showViewersSheet) {
                    detectTapGestures(
                        onPress = {
                            isPaused = true
                            tryAwaitRelease()
                            isPaused = false
                        },
                        onTap = { offset ->
                            if (offset.x < size.width / 2) {
                                // Previous story
                                if (currentIndex > 0) {
                                    currentIndex--
                                    coroutineScope.launch { progress.snapTo(0f) }
                                }
                            } else {
                                // Next story
                                if (currentIndex < stories.size - 1) {
                                    currentIndex++
                                    coroutineScope.launch { progress.snapTo(0f) }
                                } else {
                                    onClose()
                                }
                            }
                        }
                    )
                }
            }
            .testTag("story_viewer_screen")
    ) {
        // Story image
        SmartMediaImage(
            mediaUrl = currentStory.mediaUrl,
            contentDescription = currentStory.caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark gradient top and bottom overlays
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkOverlayGradient)
        )

        // Header controls (Progress bars & User Header)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp)
        ) {
            // Story Progress Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                stories.forEachIndexed { idx, _ ->
                    val segmentProgress = when {
                        idx < currentIndex -> 1f
                        idx == currentIndex -> progress.value
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { segmentProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable {
                        author?.let { onUserClick(it.id) }
                    }
                ) {
                    UserAvatar(
                        avatarUrl = author?.avatarUrl ?: "",
                        username = author?.username ?: "User",
                        size = 36.dp
                    )
                    Text(
                        text = if (isMyStory) "Your Story" else (author?.username ?: "User"),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "• Active",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close Story", tint = Color.White)
                }
            }
        }

        // Interactive Poll / Q&A Sticker Overlay & Caption
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Caption if present
            if (currentStory.caption.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = currentStory.caption,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Interactive Story Poll Widget
            var pollVoteOption by remember(currentIndex) { mutableStateOf<Int?>(null) }
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📊 Quick Poll: Love this look?",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                pollVoteOption = 1
                                Toast.makeText(context, "Voted: 🔥 100% Yes! (78% of votes)", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (pollVoteOption == 1) CrexaPurple else Color(0xFFEDE9FE),
                                contentColor = if (pollVoteOption == 1) Color.White else CrexaPurple
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (pollVoteOption != null) "🔥 78%" else "🔥 Yes", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                pollVoteOption = 2
                                Toast.makeText(context, "Voted: ✨ Super Vibe! (22% of votes)", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (pollVoteOption == 2) CrexaPurple else Color(0xFFEDE9FE),
                                contentColor = if (pollVoteOption == 2) Color.White else CrexaPurple
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (pollVoteOption != null) "✨ 22%" else "✨ Vibe", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Bottom Controls
        if (isMyStory) {
            // "Seen by" Tracker Bar for Story Creator
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clickable {
                        showViewersSheet = true
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "Seen by",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Seen by ${viewersList.size} followers",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Viewers List",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        } else {
            // Direct Reply Bar for Followers/Viewers
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(12.dp)
            ) {
                // Quick Emoji reactions
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("❤️", "🔥", "😂", "👏", "🙌", "😮").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            modifier = Modifier.clickable {
                                author?.let { onSendReply(it.id, emoji) }
                                Toast.makeText(context, "Reaction sent!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Send message...", color = Color.White.copy(alpha = 0.6f)) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    if (replyText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                author?.let { onSendReply(it.id, replyText) }
                                replyText = ""
                                Toast.makeText(context, "Reply sent!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            }
        }

        // Bottom Sheet showing followers who viewed the story
        if (showViewersSheet) {
            ModalBottomSheet(
                onDismissRequest = { showViewersSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Story Viewers (${viewersList.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    if (isLoadingViewers) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (viewersList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.People,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No views on this story yet",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(viewersList, key = { it.id }) { viewer ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showViewersSheet = false
                                            onClose()
                                            onUserClick(viewer.id)
                                        }
                                        .padding(horizontal = 20.dp, vertical = 8.dp)
                                ) {
                                    UserAvatar(
                                        avatarUrl = viewer.avatarUrl,
                                        username = viewer.username,
                                        userId = viewer.id,
                                        size = 46.dp,
                                        showRing = true
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "@${viewer.username}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = viewer.fullName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        Icons.Default.Visibility,
                                        contentDescription = "Viewed",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
