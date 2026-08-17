package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ReelEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.components.SmartMediaImage
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DarkOverlayGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReelsScreen(
    reels: List<ReelEntity>,
    users: List<UserEntity>,
    onUserClick: (String) -> Unit,
    onLikeReel: (ReelEntity) -> Unit,
    onCommentReel: (String) -> Unit,
    onSaveReel: (ReelEntity) -> Unit,
    onFollowUser: (String) -> Unit
) {
    val context = LocalContext.current
    val userMap = remember(users) { users.associateBy { it.id } }
    var selectedCategory by remember { mutableStateOf("For You") }
    val categories = listOf("For You", "Trending", "Comedy", "Tech", "Fitness", "Music", "Travel")

    val filteredReels = remember(reels, selectedCategory) {
        if (selectedCategory == "For You") {
            reels
        } else {
            val filterTag = selectedCategory.lowercase()
            val matched = reels.filter {
                it.caption.contains(filterTag, ignoreCase = true) ||
                it.audioTitle.contains(filterTag, ignoreCase = true)
            }
            if (matched.isNotEmpty()) matched else reels
        }
    }

    val pagerState = rememberPagerState(pageCount = { filteredReels.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("reels_screen")
    ) {
        if (filteredReels.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "No Reels available yet in this category.",
                    color = Color.White
                )
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val reel = filteredReels[page]
                val author = userMap[reel.userId]
                ReelPageItem(
                    reel = reel,
                    author = author,
                    isCurrentPage = pagerState.currentPage == page,
                    onUserClick = onUserClick,
                    onLikeClick = { onLikeReel(reel) },
                    onCommentClick = { onCommentReel(reel.id) },
                    onSaveClick = { onSaveReel(reel) },
                    onShareClick = {
                        val shareMessage = buildString {
                            append("Check out this reel on Crexa by @${author?.username ?: "user"}:\n")
                            if (reel.caption.isNotBlank()) {
                                append("\"${reel.caption}\"\n")
                            }
                            append("https://crexa.app/r/${reel.id}")
                        }
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, shareMessage)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Reel via")
                        context.startActivity(shareIntent)
                    },
                    onFollowClick = { author?.let { onFollowUser(it.id) } }
                )
            }
        }

        // Top Category Bar for Personalized Custom Feed
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 48.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) Color.White else Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReelPageItem(
    reel: ReelEntity,
    author: UserEntity?,
    isCurrentPage: Boolean,
    onUserClick: (String) -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var showDoubleTapHeart by remember { mutableStateOf(false) }
    var showGiftModal by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Rotating album vinyl disc animation
    val infiniteTransition = rememberInfiniteTransition(label = "disc_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        isPlaying = !isPlaying
                    },
                    onDoubleTap = {
                        if (!reel.isLiked) {
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
        // Reel Video / Thumbnail display
        SmartMediaImage(
            mediaUrl = if (reel.videoUrl.isNotBlank()) reel.videoUrl else reel.thumbnailUrl,
            contentDescription = reel.caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient dark overlay for readable text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkOverlayGradient)
        )

        // Sound Mute/Unmute & Crexa Watermark Pill on Top
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Reels",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                // Watermark Pill for Content Rights & Organic App Growth
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "⚡ Crexa • @${author?.username ?: "creator"}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            IconButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Sound Toggle",
                    tint = Color.White
                )
            }
        }

        // Pause overlay icon when tapped
        if (!isPlaying) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Paused",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        // Double Tap Like Heart Animation
        AnimatedVisibility(
            visible = showDoubleTapHeart,
            enter = scaleIn(animationSpec = spring()),
            exit = scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Liked",
                tint = Color.White.copy(alpha = 0.95f),
                modifier = Modifier.size(100.dp)
            )
        }

        // Reel Right Action Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 28.dp)
        ) {
            // Like
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier.testTag("reel_like_${reel.id}")
                ) {
                    Icon(
                        imageVector = if (reel.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (reel.isLiked) Color(0xFFEF4444) else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "${reel.likesCount}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // Comment
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onCommentClick,
                    modifier = Modifier.testTag("reel_comment_${reel.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text(
                    text = "${reel.commentsCount}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // Share
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text(
                    text = "${reel.sharesCount}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // Bookmark / Save
            IconButton(onClick = onSaveClick) {
                Icon(
                    imageVector = if (reel.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (reel.isSaved) MaterialTheme.colorScheme.primary else Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Virtual Gifts / Tip Creator
            IconButton(onClick = { showGiftModal = true }) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = "Send Gift",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Remix / Duet button
            IconButton(onClick = {
                Toast.makeText(context, "Opening Camera in Split-Screen Duet / Remix mode with @${author?.username ?: "creator"}", Toast.LENGTH_LONG).show()
            }) {
                Icon(
                    imageVector = Icons.Default.AltRoute,
                    contentDescription = "Remix / Duet",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Spinning Vinyl Album Audio Disc
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .rotate(if (isPlaying && isCurrentPage) rotationAngle else 0f)
                    .clickable {
                        Toast.makeText(context, "🎵 ${reel.audioTitle} • Used in 12.4K Crexa Reels", Toast.LENGTH_SHORT).show()
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Floating Quick Emojis Reaction Strip for Reel
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 78.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            val reelEmojis = listOf("🔥", "❤️", "😂", "👏", "🚀", "😍")
            for (em in reelEmojis) {
                Text(
                    text = em,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clickable {
                            Toast.makeText(context, "Reacted $em to @${author?.username ?: "creator"}'s reel!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        // Reel Bottom Left Details (Author, Follow button, Caption, Music title)
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.82f)
                .padding(start = 16.dp, bottom = 28.dp)
        ) {
            // Author row with follow button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UserAvatar(
                    avatarUrl = author?.avatarUrl ?: "",
                    username = author?.username ?: "User",
                    userId = author?.id ?: reel.userId,
                    size = 38.dp,
                    showRing = true,
                    onClick = { author?.let { onUserClick(it.id) } }
                )

                Text(
                    text = author?.username ?: "user",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.clickable { author?.let { onUserClick(it.id) } }
                )

                if (author?.isFollowing == false && author.isCurrentUser == false) {
                    OutlinedButton(
                        onClick = onFollowClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Follow", style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp))
                    }
                }
            }

            // Caption
            if (reel.caption.isNotBlank()) {
                Text(
                    text = reel.caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Audio track row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${reel.audioTitle} • ${reel.audioArtist}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (showGiftModal) {
            AlertDialog(
                onDismissRequest = { showGiftModal = false },
                icon = {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(32.dp))
                },
                title = {
                    Text("Send Gift to @${author?.username ?: "creator"} 🎁", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Choose a virtual gift to reward this reel:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val gifts = listOf(
                            Triple("Rose 🌹", "10 Stars", Color(0xFFEF4444)),
                            Triple("Coffee ☕", "25 Stars", Color(0xFFB45309)),
                            Triple("Rocket 🚀", "50 Stars", Color(0xFF3B82F6)),
                            Triple("Diamond 💎", "100 Stars", Color(0xFF8B5CF6)),
                            Triple("Crown 👑", "500 Stars", Color(0xFFF59E0B))
                        )
                        gifts.forEach { (name, cost, color) ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showGiftModal = false
                                        Toast.makeText(context, "🎁 Sent $name ($cost) to @${author?.username ?: "creator"}! Stars added to creator's earnings.", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Badge(containerColor = color) {
                                        Text(cost, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGiftModal = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
