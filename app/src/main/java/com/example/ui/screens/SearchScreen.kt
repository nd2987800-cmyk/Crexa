package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
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
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.components.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    posts: List<PostEntity>,
    users: List<UserEntity>,
    onUserClick: (String) -> Unit,
    onPostClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Explore Grid, 1 = Users, 2 = Tags

    val filteredUsers = remember(searchQuery, users) {
        if (searchQuery.isBlank()) users else users.filter {
            it.username.contains(searchQuery, ignoreCase = true) ||
                    it.fullName.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredPosts = remember(searchQuery, posts) {
        if (searchQuery.isBlank()) posts else posts.filter {
            it.caption.contains(searchQuery, ignoreCase = true) ||
                    it.hashtags.contains(searchQuery, ignoreCase = true) ||
                    it.location.contains(searchQuery, ignoreCase = true)
        }
    }

    val trendingTags = listOf("cyberpunk", "tokyo", "photography", "reels", "coastal", "minimalism", "coffee")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("search_screen")
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search users, posts, hashtags...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("input_search")
        )

        // Trending Tags horizontal row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(trendingTags) { tag ->
                AssistChip(
                    onClick = { searchQuery = tag },
                    label = { Text("#$tag") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (searchQuery == tag) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Explore") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Users (${filteredUsers.size})") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Hashtags") }
            )
        }

        when (selectedTab) {
            0 -> {
                // Explore Staggered Photo Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredPosts) { post ->
                        val drawableId = remember(post.mediaUrl) {
                            when {
                                post.mediaUrl.contains("img_sample_post_1") -> com.example.R.drawable.img_sample_post_1_1786177193097
                                post.mediaUrl.contains("img_sample_post_2") -> com.example.R.drawable.img_sample_post_2_1786177203745
                                post.mediaUrl.contains("img_sample_post_3") -> com.example.R.drawable.img_sample_post_3_1786177217178
                                else -> com.example.R.drawable.img_crexa_brand_logo_1786179516858
                            }
                        }

                        Box(
                            modifier = Modifier
                                .height(130.dp)
                                .clickable { onPostClick(post.id) }
                        ) {
                            Image(
                                painter = painterResource(id = drawableId),
                                contentDescription = post.caption,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (post.mediaType == "VIDEO") {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = "Reel",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
            1 -> {
                // Users search result list
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredUsers) { user ->
                        ListItem(
                            headlineContent = {
                                Text(user.username, fontWeight = FontWeight.Bold)
                            },
                            supportingContent = {
                                Text(user.fullName)
                            },
                            leadingContent = {
                                UserAvatar(
                                    avatarUrl = user.avatarUrl,
                                    username = user.username,
                                    userId = user.id,
                                    size = 46.dp,
                                    showRing = true
                                )
                            },
                            modifier = Modifier.clickable { onUserClick(user.id) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                }
            }
            2 -> {
                // Hashtags list
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(trendingTags) { tag ->
                        ListItem(
                            headlineContent = { Text("#$tag", fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("${(120..8900).random()} posts") },
                            leadingContent = {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Text("#", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            },
                            modifier = Modifier.clickable { searchQuery = tag }
                        )
                    }
                }
            }
        }
    }
}
