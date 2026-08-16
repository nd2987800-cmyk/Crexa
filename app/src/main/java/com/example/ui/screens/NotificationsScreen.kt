package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.components.SimpleTopBar
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CrexaMagenta
import com.example.ui.theme.CrexaPurple

private enum class ActivityFilter(val label: String) {
    ALL("All"),
    LIKES("Likes"),
    COMMENTS("Comments"),
    FOLLOWS("Followers")
}

@Composable
fun NotificationsScreen(
    notifications: List<NotificationEntity>,
    users: List<UserEntity>,
    posts: List<PostEntity> = emptyList(),
    onUserClick: (String) -> Unit,
    onFollowBack: (String) -> Unit,
    onPostClick: (String) -> Unit = {}
) {
    val userMap = remember(users) { users.associateBy { it.id } }
    val postMap = remember(posts) { posts.associateBy { it.id } }

    var selectedFilter by remember { mutableStateOf(ActivityFilter.ALL) }

    val filteredNotifications = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            ActivityFilter.ALL -> notifications
            ActivityFilter.LIKES -> notifications.filter { it.type == "LIKE" }
            ActivityFilter.COMMENTS -> notifications.filter { it.type == "COMMENT" }
            ActivityFilter.FOLLOWS -> notifications.filter { it.type == "FOLLOW" }
        }.sortedByDescending { it.timestamp }
    }

    val groupedNotifications = remember(filteredNotifications) {
        filteredNotifications.groupBy { getCategoryHeader(it.timestamp) }
    }

    Scaffold(
        topBar = {
            SimpleTopBar(title = "Activity")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("activity_filter_row")
            ) {
                items(ActivityFilter.values()) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                    )
                }
            }

            if (filteredNotifications.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Text(
                            text = "No activity yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "When someone likes, comments, or follows you, you'll see it here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("notifications_list")
                ) {
                    groupedNotifications.forEach { (category, notifs) ->
                        item(key = "header_$category") {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }

                        items(
                            items = notifs,
                            key = { it.id }
                        ) { notif ->
                            val actor = userMap[notif.actorUserId]
                            val post = notif.postOrReelId?.let { postMap[it] }

                            ActivityItemCard(
                                notification = notif,
                                actor = actor,
                                post = post,
                                onUserClick = { actor?.let { onUserClick(it.id) } },
                                onFollowBack = { actor?.let { onFollowBack(it.id) } },
                                onPostClick = { notif.postOrReelId?.let { onPostClick(it) } }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityItemCard(
    notification: NotificationEntity,
    actor: UserEntity?,
    post: PostEntity?,
    onUserClick: () -> Unit,
    onFollowBack: () -> Unit,
    onPostClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick() }
            .testTag("activity_item_${notification.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // Avatar with Badge Icon Overlay
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.size(52.dp)
            ) {
                UserAvatar(
                    avatarUrl = actor?.avatarUrl ?: "",
                    username = actor?.username ?: "User",
                    size = 48.dp,
                    onClick = onUserClick
                )

                // Type Icon Badge
                val (badgeBg, badgeIcon, tint) = when (notification.type) {
                    "LIKE" -> Triple(
                        Color(0xFFEF4444).copy(alpha = 0.2f),
                        Icons.Default.Favorite,
                        Color(0xFFEF4444)
                    )
                    "COMMENT" -> Triple(
                        MaterialTheme.colorScheme.primaryContainer,
                        Icons.Default.ChatBubble,
                        MaterialTheme.colorScheme.primary
                    )
                    "FOLLOW" -> Triple(
                        MaterialTheme.colorScheme.secondaryContainer,
                        Icons.Default.PersonAdd,
                        MaterialTheme.colorScheme.secondary
                    )
                    else -> Triple(
                        MaterialTheme.colorScheme.surfaceVariant,
                        Icons.Default.NotificationsNone,
                        MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .size(20.dp)
                        .offset(x = 2.dp, y = 2.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(badgeBg, CircleShape)
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = notification.type,
                            tint = tint,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Notification Content Text
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = actor?.username ?: "someone",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatTimeAgo(notification.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action / Media Preview on Right
            when (notification.type) {
                "FOLLOW" -> {
                    val isFollowing = actor?.isFollowing == true
                    if (isFollowing) {
                        OutlinedButton(
                            onClick = onFollowBack,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                "Following",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Button(
                            onClick = onFollowBack,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                "Follow",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                "LIKE", "COMMENT" -> {
                    if (post != null) {
                        val drawableId = getPostDrawableId(post.mediaUrl)
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = "Post thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onPostClick() }
                        )
                    } else {
                        Icon(
                            imageVector = if (notification.type == "LIKE") Icons.Default.Favorite else Icons.Default.ChatBubble,
                            contentDescription = null,
                            tint = if (notification.type == "LIKE") Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val diffSec = (System.currentTimeMillis() - timestamp) / 1000
    return when {
        diffSec < 60 -> "just now"
        diffSec < 3600 -> "${diffSec / 60}m ago"
        diffSec < 86400 -> "${diffSec / 3600}h ago"
        diffSec < 604800 -> "${diffSec / 86400}d ago"
        else -> "${diffSec / 604800}w ago"
    }
}

private fun getCategoryHeader(timestamp: Long): String {
    val diffHours = (System.currentTimeMillis() - timestamp) / (1000 * 3600)
    return when {
        diffHours < 24 -> "Today"
        diffHours < 168 -> "This Week"
        else -> "Earlier"
    }
}

private fun getPostDrawableId(mediaUrl: String): Int {
    return when {
        mediaUrl.contains("img_sample_post_1") -> com.example.R.drawable.img_sample_post_1_1786177193097
        mediaUrl.contains("img_sample_post_2") -> com.example.R.drawable.img_sample_post_2_1786177203745
        mediaUrl.contains("img_sample_post_3") -> com.example.R.drawable.img_sample_post_3_1786177217178
        else -> com.example.R.drawable.img_crexa_brand_logo_1786179516858
    }
}
