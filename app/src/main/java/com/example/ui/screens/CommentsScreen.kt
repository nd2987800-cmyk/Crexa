package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CommentEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.components.SimpleTopBar
import com.example.ui.components.UserAvatar

@Composable
fun CommentsScreen(
    comments: List<CommentEntity>,
    users: List<UserEntity>,
    onAddComment: (String) -> Unit,
    onLikeComment: (CommentEntity) -> Unit,
    onBackClick: () -> Unit
) {
    var commentInput by remember { mutableStateOf("") }
    val userMap = remember(users) { users.associateBy { it.id } }

    Scaffold(
        topBar = { SimpleTopBar(title = "Comments", onBackClick = onBackClick) },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        placeholder = { Text("Add a comment...") },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_comment")
                    )

                    IconButton(
                        onClick = {
                            if (commentInput.isNotBlank()) {
                                onAddComment(commentInput)
                                commentInput = ""
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("btn_post_comment")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Post", tint = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        if (comments.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text("No comments yet. Start the conversation!", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = comments,
                    key = { it.id }
                ) { comment ->
                    val author = userMap[comment.userId]
                    ListItem(
                        headlineContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(author?.username ?: "User", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("1h ago", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        supportingContent = {
                            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
                        },
                        leadingContent = {
                            UserAvatar(
                                avatarUrl = author?.avatarUrl ?: "",
                                username = author?.username ?: "User",
                                size = 40.dp
                            )
                        },
                        trailingContent = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(onClick = { onLikeComment(comment) }) {
                                    Icon(
                                        imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Like comment",
                                        tint = if (comment.isLiked) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (comment.likesCount > 0) {
                                    Text("${comment.likesCount}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                                }
                            }
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                }
            }
        }
    }
}
