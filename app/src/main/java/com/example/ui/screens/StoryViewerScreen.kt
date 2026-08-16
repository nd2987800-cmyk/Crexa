package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DarkOverlayGradient

@Composable
fun StoryViewerScreen(
    stories: List<StoryEntity>,
    users: List<UserEntity>,
    initialIndex: Int,
    onClose: () -> Unit,
    onSendReply: (recipientUserId: String, message: String) -> Unit
) {
    val context = LocalContext.current
    var currentIndex by remember { mutableStateOf(initialIndex) }
    var isPaused by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    val userMap = remember(users) { users.associateBy { it.id } }

    if (stories.isEmpty() || currentIndex >= stories.size) {
        LaunchedEffect(Unit) { onClose() }
        return
    }

    val currentStory = stories[currentIndex]
    val author = userMap[currentStory.userId]

    val progress = remember { Animatable(0f) }

    LaunchedEffect(currentIndex, isPaused) {
        if (!isPaused) {
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
            .pointerInput(currentIndex) {
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
                            }
                        } else {
                            // Next story
                            if (currentIndex < stories.size - 1) {
                                currentIndex++
                            } else {
                                onClose()
                            }
                        }
                    }
                )
            }
            .testTag("story_viewer_screen")
    ) {
        // Story image asset
        val drawableId = remember(currentStory.mediaUrl) {
            when {
                currentStory.mediaUrl.contains("img_sample_post_1") -> R.drawable.img_sample_post_1_1786177193097
                currentStory.mediaUrl.contains("img_sample_post_2") -> R.drawable.img_sample_post_2_1786177203745
                currentStory.mediaUrl.contains("img_sample_post_3") -> R.drawable.img_sample_post_3_1786177217178
                else -> R.drawable.img_lumina_logo_1786177176474
            }
        }

        Image(
            painter = painterResource(id = drawableId),
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserAvatar(
                        avatarUrl = author?.avatarUrl ?: "",
                        username = author?.username ?: "User",
                        size = 36.dp
                    )
                    Text(
                        text = author?.username ?: "User",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "• 2h",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close Story", tint = Color.White)
                }
            }
        }

        // Caption if present
        if (currentStory.caption.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(16.dp)
            ) {
                Text(
                    text = currentStory.caption,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Bottom Direct Reply Bar
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
}
