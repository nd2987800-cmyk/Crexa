package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity

@Composable
fun StoryItemRow(
    stories: List<StoryEntity>,
    users: List<UserEntity>,
    currentUser: UserEntity?,
    onStoryClick: (Int) -> Unit,
    onCreateStoryClick: () -> Unit
) {
    val userMap = users.associateBy { it.id }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("story_row")
    ) {
        // Current user "Your Story" button
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clickable { onCreateStoryClick() }
                    .testTag("add_story_item")
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    UserAvatar(
                        avatarUrl = currentUser?.avatarUrl ?: "",
                        username = currentUser?.username ?: "You",
                        userId = currentUser?.id ?: "current_user",
                        size = 62.dp,
                        showRing = true
                    )
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Story",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "Your story",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Friends stories
        itemsIndexed(stories) { index, story ->
            val author = userMap[story.userId]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clickable { onStoryClick(index) }
                    .testTag("story_item_$index")
            ) {
                UserAvatar(
                    avatarUrl = author?.avatarUrl ?: "",
                    username = author?.username ?: "User",
                    userId = author?.id ?: story.userId,
                    size = 62.dp,
                    hasStory = true,
                    storySeen = story.isSeen
                )
                Text(
                    text = author?.username ?: "user",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = if (!story.isSeen) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 68.dp)
                )
            }
        }
    }
}
