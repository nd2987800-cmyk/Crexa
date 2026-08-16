package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.components.CrexaHeaderBar
import com.example.ui.components.PostItemCard
import com.example.ui.components.StoryItemRow

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
    onOpenStory: (Int) -> Unit,
    onUserClick: (String) -> Unit,
    onLikePost: (PostEntity) -> Unit,
    onCommentPost: (String) -> Unit,
    onSavePost: (PostEntity) -> Unit,
    onReportBlockUser: (String, String) -> Unit
) {
    val context = LocalContext.current
    val userMap = remember(users) { users.associateBy { it.id } }

    Scaffold(
        topBar = {
            CrexaHeaderBar(
                onOpenSearch = onOpenSearch,
                onOpenMessages = onOpenMessages,
                onOpenCreate = onOpenCreate
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
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_feed_list")
            ) {
                // Stories Header
                item {
                    StoryItemRow(
                        stories = stories,
                        users = users,
                        currentUser = currentUser,
                        onStoryClick = onOpenStory,
                        onCreateStoryClick = onOpenCreate
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }

                // Posts List
                items(
                    items = posts,
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
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
        }
    }
}
