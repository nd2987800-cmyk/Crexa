package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.theme.CrexaPurple
import com.example.ui.theme.UserThemeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostItemCard(
    post: PostEntity,
    author: UserEntity?,
    onUserClick: (String) -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    onReportBlockUser: (String, String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showDoubleTapHeart by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val authorIdentity = remember(author?.id, author?.username) {
        UserThemeManager.getColorForUser(author?.id ?: post.userId, author?.username ?: "")
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("post_card_${post.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.clickable { author?.let { onUserClick(it.id) } }
                ) {
                    UserAvatar(
                        avatarUrl = author?.avatarUrl ?: "",
                        username = author?.username ?: "User",
                        userId = author?.id ?: post.userId,
                        size = 38.dp,
                        showRing = true
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = author?.username ?: "user",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color(0xFF0F172A)
                            )
                            if (author?.isVerified == true) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = authorIdentity.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        if (post.location.isNotBlank()) {
                            Text(
                                text = post.location,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Box {
                    IconButton(
                        onClick = { showOptionsMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = Color(0xFF64748B)
                        )
                    }
                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Not Interested") },
                            onClick = {
                                showOptionsMenu = false
                                Toast.makeText(context, "Marked as not interested", Toast.LENGTH_SHORT).show()
                            },
                            leadingIcon = { Icon(Icons.Outlined.VisibilityOff, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Hide Post") },
                            onClick = {
                                showOptionsMenu = false
                                Toast.makeText(context, "Post hidden from feed", Toast.LENGTH_SHORT).show()
                            },
                            leadingIcon = { Icon(Icons.Outlined.HideImage, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Report Post") },
                            onClick = {
                                showOptionsMenu = false
                                author?.let { onReportBlockUser("REPORT", it.id) }
                            },
                            leadingIcon = { Icon(Icons.Outlined.Report, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Mute User") },
                            onClick = {
                                showOptionsMenu = false
                                author?.let { onReportBlockUser("MUTE", it.id) }
                            },
                            leadingIcon = { Icon(Icons.Outlined.VolumeMute, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Block User", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showOptionsMenu = false
                                author?.let { onReportBlockUser("BLOCK", it.id) }
                            },
                            leadingIcon = { Icon(Icons.Outlined.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            // Post Media with subtle clean corners
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .background(Color(0xFFF8FAFC))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (!post.isLiked) {
                                    onLikeClick()
                                }
                                showDoubleTapHeart = true
                                coroutineScope.launch {
                                    delay(800)
                                    showDoubleTapHeart = false
                                }
                            }
                        )
                    }
            ) {
                SmartMediaImage(
                    mediaUrl = post.mediaUrl,
                    contentDescription = post.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Double tap like heart animation
                if (showDoubleTapHeart) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Liked",
                        tint = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier.size(96.dp)
                    )
                }
            }

            // Action Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onLikeClick,
                        modifier = Modifier.testTag("btn_like_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLiked) Color(0xFFEF4444) else Color(0xFF1E293B),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    IconButton(
                        onClick = onCommentClick,
                        modifier = Modifier.testTag("btn_comment_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.testTag("btn_share_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Send,
                            contentDescription = "Share",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onSaveClick,
                    modifier = Modifier.testTag("btn_save_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (post.isSaved) CrexaPurple else Color(0xFF1E293B),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Likes count, Floating Reactions & Caption
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)) {
                // Floating Quick Reactions Tray
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    val emojis = listOf("🔥", "❤️", "😂", "👏", "🚀", "💯")
                    for (emoji in emojis) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .clickable {
                                    Toast.makeText(context, "Reacted $emoji to @${author?.username ?: "user"}'s post!", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "${post.likesCount} likes",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (post.caption.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${author?.username ?: ""} ",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = post.caption,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1E293B)
                        )
                    }
                }

                if (post.hashtags.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = post.hashtags.split(",").joinToString(" ") { if (it.startsWith("#")) it else "#$it" },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = authorIdentity.primary
                    )
                }

                if (post.commentsCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "View all ${post.commentsCount} comments",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        modifier = Modifier.clickable { onCommentClick() }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "2 HOURS AGO",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF94A3B8)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

