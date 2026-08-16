package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.ReelEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.components.SimpleTopBar
import com.example.ui.components.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: UserEntity?,
    isCurrentUser: Boolean,
    userPosts: List<PostEntity>,
    userReels: List<ReelEntity>,
    savedPosts: List<PostEntity>,
    onEditProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFollowClick: () -> Unit,
    onMessageClick: () -> Unit,
    onPostClick: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Posts, 1 = Reels, 2 = Saved

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user?.username ?: "Profile",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        if (user?.isVerified == true) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                actions = {
                    if (isCurrentUser) {
                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier.testTag("btn_profile_settings")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Profile Header Stats & Bio
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    UserAvatar(
                        avatarUrl = user?.avatarUrl ?: "",
                        username = user?.username ?: "User",
                        size = 76.dp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        ProfileStatItem(count = user?.postsCount ?: 0, label = "Posts")
                        ProfileStatItem(count = user?.followersCount ?: 0, label = "Followers")
                        ProfileStatItem(count = user?.followingCount ?: 0, label = "Following")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user?.fullName ?: "Lumina Member",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                if (user?.bio?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (user?.website?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.website,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons Row
                if (isCurrentUser) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = onEditProfileClick,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_edit_profile")
                        ) {
                            Text("Edit Profile", fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Profile link copied!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Share Profile", fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onFollowClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user?.isFollowing == true) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                contentColor = if (user?.isFollowing == true) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (user?.isFollowing == true) "Following" else "Follow", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onMessageClick,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Message", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab Row (Posts | Reels | Saved)
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Outlined.GridOn, contentDescription = "Posts") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Outlined.Movie, contentDescription = "Reels") }
                )
                if (isCurrentUser) {
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Saved") }
                    )
                }
            }

            val displayList = when (selectedTab) {
                0 -> userPosts
                1 -> userReels.map {
                    PostEntity(
                        id = it.id,
                        userId = it.userId,
                        mediaUrl = it.thumbnailUrl,
                        caption = it.caption
                    )
                }
                else -> savedPosts
            }

            if (displayList.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Text(
                        text = when (selectedTab) {
                            0 -> "No posts shared yet"
                            1 -> "No reels posted yet"
                            else -> "No saved items"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(1.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayList) { post ->
                        val resId = remember(post.mediaUrl) {
                            when {
                                post.mediaUrl.contains("img_sample_post_1") -> R.drawable.img_sample_post_1_1786177193097
                                post.mediaUrl.contains("img_sample_post_2") -> R.drawable.img_sample_post_2_1786177203745
                                post.mediaUrl.contains("img_sample_post_3") -> R.drawable.img_sample_post_3_1786177217178
                                else -> R.drawable.img_lumina_logo_1786177176474
                            }
                        }

                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = post.caption,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .height(125.dp)
                                .clickable { onPostClick(post.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EditProfileScreen(
    currentUser: UserEntity?,
    onSaveProfile: (fullName: String, bio: String, website: String, avatarUrl: String) -> Unit,
    onBackClick: () -> Unit
) {
    var fullName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var bio by remember { mutableStateOf(currentUser?.bio ?: "") }
    var website by remember { mutableStateOf(currentUser?.website ?: "") }
    var avatarUrl by remember { mutableStateOf(currentUser?.avatarUrl ?: "") }

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = "Edit Profile",
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = { onSaveProfile(fullName, bio, website, avatarUrl) },
                        modifier = Modifier.testTag("btn_save_profile")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            UserAvatar(
                avatarUrl = avatarUrl,
                username = currentUser?.username ?: "User",
                size = 90.dp
            )

            TextButton(onClick = { /* Pick new picture */ }) {
                Text("Change profile picture", fontWeight = FontWeight.Bold)
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_edit_fullname")
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_edit_bio")
            )

            OutlinedTextField(
                value = website,
                onValueChange = { website = it },
                label = { Text("Website Link") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
