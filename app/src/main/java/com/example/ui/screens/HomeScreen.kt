package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.components.CrexaHeaderBar
import com.example.ui.components.PostItemCard
import com.example.ui.components.StoryItemRow
import com.example.ui.components.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    posts: List<PostEntity>,
    stories: List<StoryEntity>,
    users: List<UserEntity>,
    currentUser: UserEntity?,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onOpenSearch: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenCreate: () -> Unit,
    onOpenAiStudio: () -> Unit = {},
    onOpenStory: (Int) -> Unit,
    onUserClick: (String) -> Unit,
    onLikePost: (PostEntity) -> Unit,
    onCommentPost: (String) -> Unit,
    onSavePost: (PostEntity) -> Unit,
    onReportBlockUser: (String, String) -> Unit
) {
    val context = LocalContext.current
    val userMap = remember(users) { users.associateBy { it.id } }
    var feedFilter by remember { mutableStateOf("FOR_YOU") } // FOR_YOU, FOLLOWING, FAVORITES

    val filteredPosts = remember(posts, feedFilter, users) {
        when (feedFilter) {
            "FOLLOWING" -> {
                val followedIds = users.filter { it.isFollowing }.map { it.id }.toSet()
                posts.filter { it.userId in followedIds }
            }
            "FAVORITES" -> posts.filter { it.isLiked || it.isSaved }
            else -> posts
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CrexaHeaderBar(
                onOpenSearch = onOpenSearch,
                onOpenMessages = onOpenMessages,
                onOpenCreate = onOpenCreate,
                onOpenAiStudio = onOpenAiStudio
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                onRefresh()
                Toast.makeText(context, "Refreshing feed...", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .testTag("home_feed_list"),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Feed Filter Bar (For You / Following / Favorites)
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        listOf(
                            Triple("FOR_YOU", "✨ For You", null),
                            Triple("FOLLOWING", "👥 Following", null),
                            Triple("FAVORITES", "⭐ Favorites", null)
                        ).forEach { (key, label, _) ->
                            val isSelected = feedFilter == key
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { feedFilter = key }
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else Color(0xFF475569),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Stories Header
                item {
                    StoryItemRow(
                        stories = stories,
                        users = users,
                        currentUser = currentUser,
                        onStoryClick = onOpenStory,
                        onCreateStoryClick = onOpenCreate
                    )
                    HorizontalDivider(
                        color = Color(0xFFF1F5F9),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Suggested Accounts Carousel if filtered to FOLLOWING and empty
                if (feedFilter == "FOLLOWING" && filteredPosts.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Suggested Creators to Follow", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(users.take(5)) { u ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable { onUserClick(u.id) }
                                        ) {
                                            UserAvatar(avatarUrl = u.avatarUrl, username = u.username, userId = u.id, size = 50.dp)
                                            Text(u.username, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Posts List
                items(
                    items = if (filteredPosts.isEmpty() && feedFilter == "FOR_YOU") posts else filteredPosts,
                    key = { it.id }
                ) { post ->
                    val author = userMap[post.userId]
                    PostItemCard(
                        post = post,
                        author = author,
                        onUserClick = onUserClick,
                        onLikeClick = { onLikePost(post) },
                        onCommentClick = { onCommentPost(post.id) },
                        onSaveClick = { onSavePost(post) },
                        onShareClick = {
                            val shareMessage = buildString {
                                append("Check out this post on Crexa by @${author?.username ?: "user"}:\n")
                                if (post.caption.isNotBlank()) {
                                    append("\"${post.caption}\"\n")
                                }
                                append("https://crexa.app/p/${post.id}")
                            }
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Post via")
                            context.startActivity(shareIntent)
                        },
                        onReportBlockUser = onReportBlockUser
                    )
                }
            }
        }
    }
}
