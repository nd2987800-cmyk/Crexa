package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
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
import com.example.ui.components.SmartMediaImage
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
    onPostClick: (String) -> Unit,
    onFollowersClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Posts, 1 = Reels, 2 = Saved
    var showPayoutDialog by remember { mutableStateOf(false) }
    var creatorBankHolderName by remember { mutableStateOf("") }
    var creatorAccountNumber by remember { mutableStateOf("") }
    var creatorIfscCode by remember { mutableStateOf("") }
    var creatorBankName by remember { mutableStateOf("") }
    var creatorUpiId by remember { mutableStateOf("") }
    var creatorPanOrTaxId by remember { mutableStateOf("") }
    var isBankSaved by remember { mutableStateOf(false) }
    var showTipJarDialog by remember { mutableStateOf(false) }
    var tipAmountSelected by remember { mutableIntStateOf(50) } // 20, 50, 100, 500 Coins

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
                        userId = user?.id ?: "current_user",
                        size = 76.dp,
                        showRing = true
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        ProfileStatItem(count = user?.postsCount ?: 0, label = "Posts", onClick = null)
                        ProfileStatItem(count = user?.followersCount ?: 0, label = "Followers", onClick = onFollowersClick)
                        ProfileStatItem(count = user?.followingCount ?: 0, label = "Following", onClick = onFollowingClick)
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

                Spacer(modifier = Modifier.height(12.dp))

                // Professional Creator Dashboard & Insights Banner
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Toast.makeText(context, "Analytics: 14.8K Profile Views • 94% Engagement this week 📈", Toast.LENGTH_LONG).show()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Professional Dashboard", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("14.8K views in the last 30 days", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text("Insights ↗", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                if (isCurrentUser) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Creator Payouts & Earnings Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.12f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPayoutDialog = true }
                            .testTag("card_profile_payouts")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalance,
                                            contentDescription = "Payouts",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Payouts & Ad Earnings",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Badge(containerColor = Color(0xFF10B981)) {
                                            Text("Ready", color = Color.White, fontSize = 9.sp)
                                        }
                                    }
                                    Text(
                                        text = "$124.50 Accumulated • Withdraw via Bank/UPI",
                                        fontSize = 10.sp,
                                        color = Color(0xFF059669),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            FilledTonalButton(
                                onClick = { showPayoutDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Withdraw ↗", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                        FilledTonalButton(
                            onClick = { showTipJarDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFFEF3C7), contentColor = Color(0xFFB45309)),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("🎁 Tip", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Tip Jar Dialog for Creator Support
                if (showTipJarDialog) {
                    AlertDialog(
                        onDismissRequest = { showTipJarDialog = false },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎁 Support @${user?.username ?: "Creator"}", fontWeight = FontWeight.Bold)
                            }
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Send a Crexa Creator Tip badge to support their content:", fontSize = 13.sp, color = Color(0xFF475569))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(20, 50, 100, 500).forEach { coins ->
                                        val isSel = tipAmountSelected == coins
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSel) Color(0xFFF59E0B) else Color(0xFFF1F5F9),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { tipAmountSelected = coins }
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            ) {
                                                Text("🪙 $coins", fontWeight = FontWeight.Bold, color = if (isSel) Color.White else Color.Black, fontSize = 12.sp)
                                                Text("Coins", fontSize = 9.sp, color = if (isSel) Color.White.copy(alpha = 0.8f) else Color.Gray)
                                            }
                                        }
                                    }
                                }
                                Text("⭐ Creator will receive 100% of the tip directly into their settlement account.", fontSize = 11.sp, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showTipJarDialog = false
                                    Toast.makeText(context, "🎉 Sent $tipAmountSelected Coins to @${user?.username}! Thank you for supporting.", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                            ) {
                                Text("Send $tipAmountSelected Coins")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTipJarDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Story Highlights Section
                Text(
                    text = "Story Highlights",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                val sampleHighlights = listOf(
                    Pair("✨ Vibes", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=200"),
                    Pair("✈️ Travel", "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=200"),
                    Pair("🎨 Crexa AI", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200"),
                    Pair("🍕 Food", "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=200")
                )

                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isCurrentUser) {
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    Toast.makeText(context, "New Highlight created from past stories!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .border(
                                            1.5.dp,
                                            MaterialTheme.colorScheme.outlineVariant,
                                            CircleShape
                                        )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add highlight", tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("New", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    items(sampleHighlights) { (name, cover) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                Toast.makeText(context, "Opening highlight: $name", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    )
                                    .padding(3.dp)
                            ) {
                                SmartMediaImage(
                                    mediaUrl = cover,
                                    contentDescription = name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(name, fontSize = 11.sp, fontWeight = FontWeight.Medium)
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
                        SmartMediaImage(
                            mediaUrl = post.mediaUrl,
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

    if (showPayoutDialog) {
        AlertDialog(
            onDismissRequest = { showPayoutDialog = false },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF10B981),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Creator Rewards & Settlement", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Direct Bank / UPI Withdrawal", fontSize = 11.sp, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Earnings Summary Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Available Balance (12.45M Views)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$124.50 USD (₹10,395)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                }
                                Badge(containerColor = Color(0xFF10B981)) {
                                    Text("Eligible (>$100)", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Reels: 9.85M views ($98.50)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Posts: 2.60M views ($26.00)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Strict Eligibility & Policy Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Payout Rules & Guidelines", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF15803D))
                            }
                            Text("• Rate: $1.00 USD per 1,00,000 Verified Views", fontSize = 11.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                            Text("• Min Threshold: $100.00 USD (Requirement met ✅)", fontSize = 11.sp, color = Color(0xFF166534))
                            Text("• Direct Settlement via NEFT / RTGS / IMPS / UPI", fontSize = 11.sp, color = Color(0xFF166534))
                        }
                    }

                    Text("Beneficiary Bank Account Details (KYC)", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    OutlinedTextField(
                        value = creatorBankHolderName,
                        onValueChange = { creatorBankHolderName = it },
                        label = { Text("Account Holder Name (As per Bank)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = creatorAccountNumber,
                        onValueChange = { creatorAccountNumber = it },
                        label = { Text("Bank Account Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = creatorIfscCode,
                        onValueChange = { creatorIfscCode = it.uppercase() },
                        label = { Text("IFSC / Swift Code (e.g. HDFC0001234)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = creatorBankName,
                        onValueChange = { creatorBankName = it },
                        label = { Text("Bank Name (e.g. HDFC / SBI / ICICI)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = creatorUpiId,
                        onValueChange = { creatorUpiId = it },
                        label = { Text("UPI ID (Optional, e.g. name@okhdfcbank)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = creatorPanOrTaxId,
                        onValueChange = { creatorPanOrTaxId = it.uppercase() },
                        label = { Text("PAN Card / Tax ID (For TDS Compliance)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (creatorAccountNumber.isNotBlank() && creatorIfscCode.isNotBlank() && creatorBankHolderName.isNotBlank()) {
                            isBankSaved = true
                            showPayoutDialog = false
                            Toast.makeText(
                                context,
                                "✅ Withdrawal Request of $124.50 submitted! Bank settlement will be processed to $creatorBankName within 24-48 hours.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(context, "Please fill in Account Name, Number & IFSC code", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Request Payout ($124.50)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPayoutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ProfileStatItem(count: Int, label: String, onClick: (() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
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
    val context = LocalContext.current
    var fullName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var bio by remember { mutableStateOf(currentUser?.bio ?: "") }
    var website by remember { mutableStateOf(currentUser?.website ?: "") }
    var avatarUrl by remember { mutableStateOf(currentUser?.avatarUrl ?: "") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            avatarUrl = uri.toString()
            Toast.makeText(context, "Profile photo selected!", Toast.LENGTH_SHORT).show()
        }
    }

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
                userId = currentUser?.id ?: "current_user",
                size = 90.dp,
                showRing = true
            )

            TextButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
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

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider()

            Text(
                text = "Account Information",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = currentUser?.email?.ifBlank { "Not configured" } ?: "Not configured",
                onValueChange = {},
                readOnly = true,
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = currentUser?.phoneNumber?.ifBlank { "No phone number linked" } ?: "No phone number linked",
                onValueChange = {},
                readOnly = true,
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
