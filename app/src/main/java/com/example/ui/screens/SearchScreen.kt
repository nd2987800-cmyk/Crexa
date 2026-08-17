package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.components.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    posts: List<PostEntity>,
    users: List<UserEntity>,
    currentUser: UserEntity? = null,
    firestoreSearchResults: List<UserEntity> = emptyList(),
    isSearchingUsers: Boolean = false,
    onSearchUsers: (String) -> Unit = {},
    onFollowUser: (String) -> Unit = {},
    onUserClick: (String) -> Unit,
    onPostClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(1) } // Default to Users tab for easy friend discovery

    // Trigger Firestore search whenever query changes
    LaunchedEffect(searchQuery) {
        onSearchUsers(searchQuery)
    }

    // Merge and deduplicate Firestore search results with cached users
    val combinedUsers = remember(searchQuery, users, firestoreSearchResults, currentUser) {
        val list = mutableListOf<UserEntity>()
        val seenIds = mutableSetOf<String>()

        // 1. Add direct Firestore search hits first
        for (u in firestoreSearchResults) {
            if (seenIds.add(u.id)) {
                // sync local follow status if available
                val localMatch = users.find { it.id == u.id }
                list.add(if (localMatch != null) u.copy(isFollowing = localMatch.isFollowing) else u)
            }
        }

        // 2. Add local matches from cache
        if (searchQuery.isNotBlank()) {
            val localMatches = users.filter {
                it.username.contains(searchQuery, ignoreCase = true) ||
                        it.fullName.contains(searchQuery, ignoreCase = true)
            }
            for (u in localMatches) {
                if (seenIds.add(u.id)) {
                    list.add(u)
                }
            }
        } else {
            // When query is empty, show suggested creators from cached users
            for (u in users) {
                if (u.id != currentUser?.id && seenIds.add(u.id)) {
                    list.add(u)
                }
            }
        }

        list
    }

    val filteredPosts = remember(searchQuery, posts) {
        if (searchQuery.isBlank()) posts else posts.filter {
            it.caption.contains(searchQuery, ignoreCase = true) ||
                    it.hashtags.contains(searchQuery, ignoreCase = true) ||
                    it.location.contains(searchQuery, ignoreCase = true)
        }
    }

    val trendingTags = listOf("crexa", "photography", "tokyo", "reels", "cyberpunk", "minimalism", "design", "tech", "fashion")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("search_screen")
    ) {
        // Top Search Bar Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search @username, name, or tags...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search")
            )
        }

        // Search Loading Progress Bar
        AnimatedVisibility(
            visible = isSearchingUsers,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Trending Tags row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(trendingTags) { tag ->
                val isSelected = searchQuery == tag
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        searchQuery = if (isSelected) "" else tag
                        selectedTab = if (isSelected) 1 else 0
                    },
                    label = { Text("#$tag", fontSize = 13.sp) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        // Navigation Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Explore", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        if (searchQuery.isNotBlank()) "Users (${combinedUsers.size})" else "Find Friends",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                icon = { Icon(Icons.Default.PersonSearch, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Trending Audio 🎵", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Hashtags", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Tag, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        when (selectedTab) {
            0 -> {
                // Explore Grid Tab
                if (filteredPosts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No posts found for \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredPosts, key = { it.id }) { post ->
                            Box(
                                modifier = Modifier
                                    .height(130.dp)
                                    .clickable { onPostClick(post.id) }
                            ) {
                                if (post.mediaUrl.startsWith("http") || post.mediaUrl.startsWith("content://") || post.mediaUrl.startsWith("file://")) {
                                    AsyncImage(
                                        model = post.mediaUrl,
                                        contentDescription = post.caption,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
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
                                }

                                if (post.mediaType == "VIDEO") {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.5f))
                                            .padding(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Movie,
                                            contentDescription = "Reel",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Users / Find Friends Tab
                if (combinedUsers.isEmpty() && !isSearchingUsers) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.PersonOff,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                if (searchQuery.isNotBlank()) "No users found on Crexa for \"$searchQuery\"" else "No users discovered yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Try searching with a username or handle like @alex, @elena, or full name.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        if (searchQuery.isBlank()) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Suggested Friends & Creators",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else {
                            item {
                                Text(
                                    "Firestore Search Results (${combinedUsers.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }

                        items(combinedUsers, key = { it.id }) { user ->
                            val isMe = user.id == currentUser?.id
                            UserSearchItem(
                                user = user,
                                isCurrentUser = isMe,
                                onFollowClick = { onFollowUser(user.id) },
                                onUserClick = { onUserClick(user.id) }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                modifier = Modifier.padding(start = 72.dp)
                            )
                        }
                    }
                }
            }

            2 -> {
                // Trending Audio / Sounds Discover Tab
                val trendingSounds = listOf(
                    Triple("Cyber Dreams (Synthwave Mix)", "128.4K Reels", "🎧 Neon Drift"),
                    Triple("Tokyo Night Drive (Speed Up)", "89.2K Reels", "🔥 Lofi Beats"),
                    Triple("Golden Hour Sunset Vibes", "64.1K Reels", "✨ Acoustic Soul"),
                    Triple("Crexa Viral Bass Booster", "215.8K Reels", "⚡ Crexa Original"),
                    Triple("Chill Summer Breeze", "45.7K Reels", "🌊 Ocean Waves"),
                    Triple("Deep Focus & Coding Beat", "38.9K Reels", "💻 ByteCraft")
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Trending Soundtracks", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Pick a viral audio sound to create your next Reel", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    items(trendingSounds) { (title, count, artist) ->
                        ListItem(
                            headlineContent = { Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                            supportingContent = { Text("$artist • $count", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary) },
                            leadingContent = {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                            },
                            trailingContent = {
                                Button(
                                    onClick = {},
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.MovieCreation, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Use Audio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    }
                }
            }

            3 -> {
                // Hashtags tab
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(trendingTags) { tag ->
                        ListItem(
                            headlineContent = { Text("#$tag", fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("Discover trending content with #$tag", fontSize = 13.sp) },
                            leadingContent = {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Text(
                                        "#",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp
                                    )
                                }
                            },
                            trailingContent = {
                                Icon(Icons.Default.ChevronRight, contentDescription = "View tag", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            modifier = Modifier.clickable {
                                searchQuery = tag
                                selectedTab = 0
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    }
                }
            }
        }
    }
}

@Composable
private fun UserSearchItem(
    user: UserEntity,
    isCurrentUser: Boolean,
    onFollowClick: () -> Unit,
    onUserClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("user_item_${user.username}")
    ) {
        UserAvatar(
            avatarUrl = user.avatarUrl,
            username = user.username,
            userId = user.id,
            size = 50.dp,
            showRing = true
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "@${user.username}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (user.followersCount > 100) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Text(
                text = user.fullName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (user.bio.isNotBlank()) {
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "${user.followersCount} followers",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        if (isCurrentUser) {
            SuggestionChip(
                onClick = onUserClick,
                label = { Text("You", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                shape = RoundedCornerShape(16.dp)
            )
        } else {
            Button(
                onClick = onFollowClick,
                colors = if (user.isFollowing) {
                    ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("btn_follow_${user.id}")
            ) {
                if (user.isFollowing) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Following", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Follow", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
