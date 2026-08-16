package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.UserEntity
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
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showDoubleTapHeart by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("post_card_${post.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.clickable { author?.let { onUserClick(it.id) } }
                ) {
                    UserAvatar(
                        avatarUrl = author?.avatarUrl ?: "",
                        username = author?.username ?: "User",
                        size = 38.dp
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = author?.username ?: "unknown",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (author?.isVerified == true) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        if (post.location.isNotBlank()) {
                            Text(
                                text = post.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
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

            // Post Media (Double tap to like)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .background(Color.Black)
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
                // Display photo or sample asset
                val drawableId = remember(post.mediaUrl) {
                    when {
                        post.mediaUrl.contains("img_sample_post_1") -> com.example.R.drawable.img_sample_post_1_1786177193097
                        post.mediaUrl.contains("img_sample_post_2") -> com.example.R.drawable.img_sample_post_2_1786177203745
                        post.mediaUrl.contains("img_sample_post_3") -> com.example.R.drawable.img_sample_post_3_1786177217178
                        else -> com.example.R.drawable.img_crexa_brand_logo_1786179516858
                    }
                }

                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = post.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Double tap like heart animation
                androidx.compose.animation.AnimatedVisibility(
                    visible = showDoubleTapHeart,
                    enter = scaleIn(animationSpec = spring()),
                    exit = scaleOut()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Liked",
                        tint = Color.White.copy(alpha = 0.9f),
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
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onLikeClick,
                        modifier = Modifier.testTag("btn_like_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLiked) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onCommentClick,
                        modifier = Modifier.testTag("btn_comment_${post.id}")
                    ) {
                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comments")
                    }
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.testTag("btn_share_${post.id}")
                    ) {
                        Icon(Icons.Outlined.Send, contentDescription = "Share")
                    }
                }

                IconButton(
                    onClick = onSaveClick,
                    modifier = Modifier.testTag("btn_save_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (post.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Likes count & Caption
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)) {
                Text(
                    text = "${post.likesCount} likes",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (post.caption.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${author?.username ?: ""} ",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = post.caption,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (post.hashtags.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = post.hashtags.split(",").joinToString(" ") { if (it.startsWith("#")) it else "#$it" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (post.commentsCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "View all ${post.commentsCount} comments",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onCommentClick() }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "2 HOURS AGO",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
