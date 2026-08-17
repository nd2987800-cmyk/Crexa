package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
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
import com.example.ui.components.SimpleTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentDarkMode: Boolean?,
    onSetThemeMode: (Boolean?) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: (() -> Unit)? = null,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var isPrivateAccount by remember { mutableStateOf(false) }
    var isBiometricLockEnabled by remember { mutableStateOf(false) }
    var isAntiScreenshotProtected by remember { mutableStateOf(true) }
    var showSessionsDialog by remember { mutableStateOf(false) }
    var showTwoFactorDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportText by remember { mutableStateOf("") }
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    var isSubscribed by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showSavedCollectionsDialog by remember { mutableStateOf(false) }
    var selectedSavedTab by remember { mutableIntStateOf(0) } // 0: Posts, 1: Reels, 2: Audio Tracks
    var showCreatorPartnerDialog by remember { mutableStateOf(false) }
    var showCreatorAcademyDialog by remember { mutableStateOf(false) }
    var showCoinsWalletDialog by remember { mutableStateOf(false) }
    var showReferralDialog by remember { mutableStateOf(false) }
    var showLeaderboardDialog by remember { mutableStateOf(false) }
    var showPayoutPolicyDialog by remember { mutableStateOf(false) }
    var showInvoicesDialog by remember { mutableStateOf(false) }
    var showAdminPayoutDashboard by remember { mutableStateOf(false) }
    var userCoinsBalance by remember { mutableIntStateOf(250) }
    var creatorBankHolderName by remember { mutableStateOf("") }
    var creatorAccountNumber by remember { mutableStateOf("") }
    var creatorIfscCode by remember { mutableStateOf("") }
    var creatorBankName by remember { mutableStateOf("") }
    var creatorUpiId by remember { mutableStateOf("") }
    var creatorPanOrTaxId by remember { mutableStateOf("") }
    var isBankSaved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = "Settings",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Subscription & Monetization Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Subscription",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "Crexa VIP & AI Subscription",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isSubscribed) "Active VIP Subscriber • Unlimited AI" else "50% Creator / 50% Platform Split",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        text = "Unlock 4K AI Generation, Crexa VIP Verified Badge, Ad-Free browsing, and Priority Server Speed. Google Play In-App Billing ready.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { showSubscriptionDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_settings_subscription")
                    ) {
                        Text(if (isSubscribed) "Manage VIP Subscription" else "Subscribe for $4.99/mo (50-50 Split)")
                    }
                }
            }

            // Creator Monetization & Direct Bank Settlement
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = "Bank Payouts",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Creator Rewards & Bank Settlement",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                text = "1,000+ Followers • Real Money: $1 per 100,000 Views",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF059669),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = "• Rate: Approx $1.00 per 1,00,000 Genuine Views (RPM = $0.01)\n• Eligibility: Minimum 1,000 Real Followers\n• Strict Bank KYC & Manual Bank Settlement directly by Admin\n• Minimum Payout Threshold: $100.00 (Requires ~10M cumulative views)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { showCreatorPartnerDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBankSaved) "View Bank Account & Earnings ($124.50)" else "Setup Creator Bank Settlement ($100 Min)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Saved Content & Your Activity
            Text(
                text = "Creator Hub & Activity",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )

            // Admin Payout Management Dashboard Entry
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { showAdminPayoutDashboard = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF3B82F6).copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin",
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Admin Payout Hub 👑", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(containerColor = Color(0xFFEF4444)) {
                                    Text("2 Pending", color = Color.White, fontSize = 10.sp)
                                }
                            }
                            Text("Review creator requests, verify KYC & send bank transfers", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                }
            }

            ListItem(
                headlineContent = { Text("Creator Academy & Viral Playbook 🎓", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Guidelines, algorithmic growth tips, best posting times & monetization rules") },
                leadingContent = { Icon(Icons.Default.School, contentDescription = "Academy", tint = MaterialTheme.colorScheme.primary) },
                trailingContent = {
                    FilledTonalButton(
                        onClick = { showCreatorAcademyDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Open Playbook", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.clickable { showCreatorAcademyDialog = true }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Crexa Stars & Gift Wallet 🪙", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Current Balance: $userCoinsBalance Stars • Send gifts or cash out tips") },
                leadingContent = { Icon(Icons.Default.CardGiftcard, contentDescription = "Wallet", tint = Color(0xFFF59E0B)) },
                trailingContent = {
                    FilledTonalButton(
                        onClick = { showCoinsWalletDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Manage / Buy", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.clickable { showCoinsWalletDialog = true }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Referral Program & Free Stars 🎁", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Invite friends with your link & earn 50 Crexa Stars per install") },
                leadingContent = { Icon(Icons.Default.Share, contentDescription = "Refer", tint = Color(0xFF10B981)) },
                trailingContent = {
                    FilledTonalButton(
                        onClick = { showReferralDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Invite & Earn", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.clickable { showReferralDialog = true }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Weekly Creator Leaderboard 🏆", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Top 10 creators with highest reach, views & engagement awards") },
                leadingContent = { Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard", tint = Color(0xFFF59E0B)) },
                trailingContent = {
                    FilledTonalButton(
                        onClick = { showLeaderboardDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Rankings", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.clickable { showLeaderboardDialog = true }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Bank Settlement Invoices & TDS 📄", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Download official payment receipts, TDS certificates & UTR logs") },
                leadingContent = { Icon(Icons.Default.ReceiptLong, contentDescription = "Invoices", tint = MaterialTheme.colorScheme.primary) },
                trailingContent = {
                    FilledTonalButton(
                        onClick = { showInvoicesDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Invoices", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.clickable { showInvoicesDialog = true }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Creator Monetization & Legal Policy ⚖️", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("View anti-fraud terms, 18+ age verification & payout compliance") },
                leadingContent = { Icon(Icons.Default.Gavel, contentDescription = "Legal", tint = Color(0xFF6B7280)) },
                trailingContent = {
                    FilledTonalButton(
                        onClick = { showPayoutPolicyDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("View Terms", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.clickable { showPayoutPolicyDialog = true }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Saved / Bookmarks 🔖", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("View your saved Posts, Reels, and Saved Audio Tracks") },
                leadingContent = { Icon(Icons.Default.Bookmark, contentDescription = "Saved", tint = MaterialTheme.colorScheme.primary) },
                trailingContent = {
                    Button(
                        onClick = { showSavedCollectionsDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("View Saved", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.clickable { showSavedCollectionsDialog = true }
            )

            HorizontalDivider()

            // Appearance section
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Dark Theme") },
                supportingContent = {
                    Text(
                        when (currentDarkMode) {
                            true -> "Dark Mode enabled"
                            false -> "Light Mode enabled"
                            else -> "Following system theme"
                        }
                    )
                },
                leadingContent = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = currentDarkMode == true,
                        onCheckedChange = { isChecked ->
                            onSetThemeMode(if (isChecked) true else false)
                        },
                        modifier = Modifier.testTag("switch_dark_mode")
                    )
                }
            )

            HorizontalDivider()

            // Account & Privacy Section
            Text(
                text = "Privacy, Security & Safety",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Private Account") },
                supportingContent = { Text("Only approved followers can see your posts & reels") },
                leadingContent = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = isPrivateAccount,
                        onCheckedChange = {
                            isPrivateAccount = it
                            Toast.makeText(context, if (it) "Account is now Private 🔒" else "Account is now Public 🌍", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Biometric & App Lock") },
                supportingContent = { Text("Require Fingerprint, Face, or PIN when opening Crexa") },
                leadingContent = { Icon(Icons.Outlined.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingContent = {
                    Switch(
                        checked = isBiometricLockEnabled,
                        onCheckedChange = {
                            isBiometricLockEnabled = it
                            Toast.makeText(context, if (it) "Biometric App Lock Enabled 🛡️" else "App Lock Disabled", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Anti-Screenshot Privacy Mode") },
                supportingContent = { Text("Allowed only on Reels & Feed; chats/settings are protected") },
                leadingContent = { Icon(Icons.Outlined.Screenshot, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingContent = {
                    Badge(containerColor = Color(0xFF10B981)) {
                        Text("Active (FLAG_SECURE)", color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            )

            ListItem(
                headlineContent = { Text("Two-Factor Authentication (2FA)") },
                supportingContent = { Text("Authenticator App & Backup Codes for secure login") },
                leadingContent = { Icon(Icons.Outlined.Security, contentDescription = null) },
                trailingContent = {
                    TextButton(onClick = { showTwoFactorDialog = true }) {
                        Text("Setup ↗", fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.clickable { showTwoFactorDialog = true }
            )

            ListItem(
                headlineContent = { Text("Active Devices & Login Sessions") },
                supportingContent = { Text("Review where your account is currently logged in") },
                leadingContent = { Icon(Icons.Outlined.Devices, contentDescription = null) },
                trailingContent = {
                    TextButton(onClick = { showSessionsDialog = true }) {
                        Text("Manage", fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.clickable { showSessionsDialog = true }
            )

            var isCommentFilterEnabled by remember { mutableStateOf(true) }
            ListItem(
                headlineContent = { Text("Offensive Comment Filtering (AI)") },
                supportingContent = { Text("Auto-hide abusive comments and spam with Gemini AI") },
                leadingContent = { Icon(Icons.Outlined.Shield, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = isCommentFilterEnabled,
                        onCheckedChange = {
                            isCommentFilterEnabled = it
                            Toast.makeText(context, if (it) "AI Comment Filter is Active 🛡️" else "Comment filter turned off", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Close Friends List") },
                supportingContent = { Text("Manage 12 close friends for private stories") },
                leadingContent = { Icon(Icons.Outlined.StarOutline, contentDescription = null) },
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Close Friends List: 12 members selected 🟢", Toast.LENGTH_SHORT).show()
                }
            )

            ListItem(
                headlineContent = { Text("Blocked Users") },
                leadingContent = { Icon(Icons.Outlined.Block, contentDescription = null) },
                modifier = Modifier.clickable {
                    Toast.makeText(context, "No blocked users", Toast.LENGTH_SHORT).show()
                }
            )

            ListItem(
                headlineContent = { Text("Muted Accounts") },
                leadingContent = { Icon(Icons.Outlined.VolumeMute, contentDescription = null) },
                modifier = Modifier.clickable {
                    Toast.makeText(context, "No muted accounts", Toast.LENGTH_SHORT).show()
                }
            )

            HorizontalDivider()

            // Support section
            Text(
                text = "Support & Policies",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Report an Issue") },
                leadingContent = { Icon(Icons.Outlined.Report, contentDescription = null) },
                modifier = Modifier.clickable { showReportDialog = true }
            )

            ListItem(
                headlineContent = { Text("About Crexa") },
                supportingContent = { Text("Version 1.0.0 • Crexa Social & AI Platform") },
                leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) }
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp)
                    .testTag("btn_logout")
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Delete Account Button
            OutlinedButton(
                onClick = { showDeleteConfirmDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp)
                    .testTag("btn_delete_account")
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Profile & Data", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSubscriptionDialog) {
        AlertDialog(
            onDismissRequest = { showSubscriptionDialog = false },
            icon = { Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) },
            title = { Text("Crexa VIP Subscription") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Price: $4.99 / Month",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("• 50% Revenue goes to your Creator Account Wallet")
                    Text("• 50% Platform & Google Cloud Infrastructure fee")
                    Text("• Unlimited Crexa AI Studio Generations & Video FX")
                    Text("• Golden Verified Badge on Profile & Reels")
                    Text("• Priority Chat & Direct Message Delivery")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSubscribed = true
                        showSubscriptionDialog = false
                        Toast.makeText(context, "VIP Subscription Activated! 50% Share Configured.", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text(if (isSubscribed) "Subscription Active" else "Confirm & Subscribe")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubscriptionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Profile?") },
            text = {
                Text("Are you sure you want to delete your profile and remove all stored password credentials? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        if (onDeleteAccount != null) {
                            onDeleteAccount()
                        } else {
                            onLogout()
                        }
                        Toast.makeText(context, "Profile deleted successfully.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Account", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Keep Account")
                }
            }
        )
    }

    if (showTwoFactorDialog) {
        AlertDialog(
            onDismissRequest = { showTwoFactorDialog = false },
            icon = { Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) },
            title = { Text("Two-Factor Authentication (2FA)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Protect your Crexa account with an extra security step on new logins.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Secret Key for Authenticator:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("CRX9 - 84KJ - 22PL - MN90", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text("• Google Authenticator / Authy support", fontSize = 12.sp)
                    Text("• SMS & WhatsApp verification codes", fontSize = 12.sp)
                    Text("• 8 Emergency backup codes generated", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTwoFactorDialog = false
                        Toast.makeText(context, "2FA has been activated! 🛡️", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Activate 2FA")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTwoFactorDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showSessionsDialog) {
        AlertDialog(
            onDismissRequest = { showSessionsDialog = false },
            icon = { Icon(Icons.Default.Devices, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) },
            title = { Text("Active Login Sessions") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("You are currently signed in on these devices:", style = MaterialTheme.typography.bodySmall)

                    // Session 1: Current Device
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Android Device", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Badge(containerColor = Color(0xFF10B981)) { Text("This Device", color = Color.White, fontSize = 9.sp) }
                                }
                                Text("New Delhi, India • Active Now", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Session 2: Web Session
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Computer, contentDescription = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Chrome on Windows", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Mumbai, India • Active 2 hours ago", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSessionsDialog = false
                        Toast.makeText(context, "Logged out from all other 1 device.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log Out Other Devices", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSessionsDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    if (showCreatorPartnerDialog) {
        AlertDialog(
            onDismissRequest = { showCreatorPartnerDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Creator Rewards & Settlement", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Real Money Payouts via Direct Bank/UPI", fontSize = 11.sp, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Strict Eligibility & Policy Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Strict Partner Guidelines & Rules", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF15803D))
                            }
                            Text("1. Eligibility: Minimum 1,000 Real Followers required.", fontSize = 11.sp, color = Color(0xFF166534))
                            Text("2. View-Based Earnings Rate: $1.00 USD per 1,00,000 (1 Lakh) Genuine Views.", fontSize = 11.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                            Text("3. Minimum Payout Threshold: $100.00 (Payout requires at least $100.00 balance).", fontSize = 11.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                            Text("4. Strict Anti-Fraud: Fake bot traffic or loop scripting will lead to immediate ban.", fontSize = 11.sp, color = Color(0xFF991B1B))
                            Text("5. Direct Settlement: Bank transfers/UPI dispatched directly by Admin each month.", fontSize = 11.sp, color = Color(0xFF166534))
                        }
                    }

                    // Earnings Status & Views Breakdown Summary
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
                                    Text("Current Balance (12.45M Views)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$124.50 USD", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                }
                                Badge(containerColor = Color(0xFF10B981)) {
                                    Text("Eligible for Payout (>$100)", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Reels Views: 9,850,000", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Post Views: 2,600,000", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Text("Beneficiary Bank Account Details (Strict KYC)", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    OutlinedTextField(
                        value = creatorBankHolderName,
                        onValueChange = { creatorBankHolderName = it },
                        label = { Text("Account Holder Full Name (As per Bank/PAN)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = creatorAccountNumber,
                        onValueChange = { creatorAccountNumber = it },
                        label = { Text("Bank Account Number / IBAN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = creatorIfscCode,
                            onValueChange = { creatorIfscCode = it.uppercase() },
                            label = { Text("IFSC / SWIFT Code") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = creatorBankName,
                            onValueChange = { creatorBankName = it },
                            label = { Text("Bank Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = creatorUpiId,
                        onValueChange = { creatorUpiId = it },
                        label = { Text("UPI ID / PayPal Email (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = creatorPanOrTaxId,
                        onValueChange = { creatorPanOrTaxId = it.uppercase() },
                        label = { Text("PAN Number / Tax ID (Strict Verification)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (creatorBankHolderName.isBlank() || creatorAccountNumber.isBlank() || creatorIfscCode.isBlank()) {
                            Toast.makeText(context, "Please fill required bank details accurately!", Toast.LENGTH_SHORT).show()
                        } else {
                            isBankSaved = true
                            showCreatorPartnerDialog = false
                            Toast.makeText(context, "Bank Settlement Details Saved & Verified! Admin payout ready ($100+ min).", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Save & Request Settlement", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatorPartnerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCreatorAcademyDialog) {
        AlertDialog(
            onDismissRequest = { showCreatorAcademyDialog = false },
            icon = {
                Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Creator Academy & Growth Playbook 🎓", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("⚡ 1. The 3-Second Viral Hook Rule", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("The Crexa recommendation engine checks the first 3 seconds. Hook viewers immediately with bold captions, visual transitions, or surprising intros to retain >65% watch-time.", fontSize = 11.sp)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🕒 2. Prime Posting Hours (IST & Global)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Peak active audience windows: \n• Evening: 6:00 PM – 9:30 PM (Best for Reels)\n• Afternoon: 12:30 PM – 2:00 PM (Lunch break)\n• Sunday Mornings: 10:00 AM – 1:00 PM", fontSize = 11.sp)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🎵 3. Leverage Trending Sounds & Audio", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Reels created with trending vinyl tracks receive 2.4x higher organic impressions on the Explore and Reels feed.", fontSize = 11.sp)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("⚠️ 4. Anti-Fraud & Strict Ban Policy", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF991B1B))
                            Text("Automated click farms, emulator bot loops, and re-uploading uncredited copyrighted media are auto-flagged by server moderators and lead to instant disqualification from monetisation payouts.", fontSize = 11.sp, color = Color(0xFF7F1D1D))
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showCreatorAcademyDialog = false }) {
                    Text("Got It! 🚀")
                }
            }
        )
    }

    if (showCoinsWalletDialog) {
        AlertDialog(
            onDismissRequest = { showCoinsWalletDialog = false },
            icon = {
                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(36.dp))
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Crexa Stars & Gift Wallet", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Support your favorite creators with live virtual gifts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Your Wallet Balance", fontSize = 11.sp, color = Color(0xFF92400E))
                                Text("$userCoinsBalance Stars 🪙", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            }
                            Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(32.dp))
                        }
                    }

                    Text("Recharge Star Packs", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    // Pack 1
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("100 Crexa Stars 🪙", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Starter Pack", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = {
                                    userCoinsBalance += 100
                                    Toast.makeText(context, "Added 100 Stars to Wallet! New Balance: $userCoinsBalance", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("$0.99", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Pack 2
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("550 Stars (50 Bonus! 🔥)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Popular Choice", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = {
                                    userCoinsBalance += 550
                                    Toast.makeText(context, "Added 550 Stars to Wallet! New Balance: $userCoinsBalance", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("$3.99", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Pack 3
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("1,400 Stars (200 Bonus! 👑)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("VIP Creator Booster", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = {
                                    userCoinsBalance += 1400
                                    Toast.makeText(context, "Added 1400 Stars to Wallet! New Balance: $userCoinsBalance", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("$8.99", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showCoinsWalletDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    if (showSavedCollectionsDialog) {
        AlertDialog(
            onDismissRequest = { showSavedCollectionsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saved Collections 🔖", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    PrimaryTabRow(
                        selectedTabIndex = selectedSavedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedSavedTab == 0,
                            onClick = { selectedSavedTab = 0 },
                            text = { Text("Posts (4)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedSavedTab == 1,
                            onClick = { selectedSavedTab = 1 },
                            text = { Text("Reels (3)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedSavedTab == 2,
                            onClick = { selectedSavedTab = 2 },
                            text = { Text("Audio (5)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    when (selectedSavedTab) {
                        0 -> {
                            // Saved Posts Tab
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Cyberpunk City Glow in Tokyo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("by @alex_chen • Saved yesterday", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Minimalist Architecture Workspace", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("by @elena_v • Saved 3 days ago", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Saved Reels Tab
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.MovieCreation, contentDescription = null, tint = Color(0xFFEF4444))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Top 5 Android Compose Animations", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("by @tech_guru • 84K Views", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.MovieCreation, contentDescription = null, tint = Color(0xFFEF4444))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Sunset in Santorini 4K Drone Reel", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("by @wanderlust • 120K Views", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Saved Audio / Songs Tab
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Cyber Dreams (Synthwave Mix)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("🎧 Neon Drift • Used in 128K Reels", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                        TextButton(onClick = { Toast.makeText(context, "Ready to use in Camera Reel!", Toast.LENGTH_SHORT).show() }) {
                                            Text("Use", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Tokyo Night Drive (Speed Up)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("🔥 Lofi Beats • Used in 89K Reels", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                        TextButton(onClick = { Toast.makeText(context, "Ready to use in Camera Reel!", Toast.LENGTH_SHORT).show() }) {
                                            Text("Use", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSavedCollectionsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showReferralDialog) {
        AlertDialog(
            onDismissRequest = { showReferralDialog = false },
            icon = {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Invite Friends & Earn Free Stars 🎁", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Share your unique referral link with creators & friends. Whenever someone registers using your invite, both of you receive 50 Stars instantly.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Your Referral Code", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("CREXA-VIP778", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF10B981))
                            }
                            IconButton(onClick = {
                                Toast.makeText(context, "Referral Link & Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Invited Friends: 14", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Stars Earned: 700 🪙", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    Toast.makeText(context, "Opening share sheet: https://crexa.app/join/CREXA-VIP778", Toast.LENGTH_SHORT).show()
                    showReferralDialog = false
                }) {
                    Text("Share Invite Link 🚀")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReferralDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showLeaderboardDialog) {
        AlertDialog(
            onDismissRequest = { showLeaderboardDialog = false },
            icon = {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Weekly Creator Leaderboard 🏆", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Top creators by weekly engagement & verified views:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    val topCreators = listOf(
                        Triple("🥇 @aarav_vlogs", "3.4M Views • 14.2K Gifts", Color(0xFFF59E0B)),
                        Triple("🥈 @priya_creatives", "2.8M Views • 11.5K Gifts", Color(0xFF94A3B8)),
                        Triple("🥉 @rohit_fitness", "2.1M Views • 9.8K Gifts", Color(0xFFB45309)),
                        Triple("4. @sneha_travels", "1.7M Views • 7.4K Gifts", Color.Gray),
                        Triple("5. @tech_raj", "1.2M Views • 5.1K Gifts", Color.Gray)
                    )

                    topCreators.forEach { (name, stats, badgeColor) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(stats, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Surface(shape = CircleShape, color = badgeColor.copy(alpha = 0.2f)) {
                                    Icon(Icons.Default.Stars, contentDescription = null, tint = badgeColor, modifier = Modifier.padding(6.dp).size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showLeaderboardDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showInvoicesDialog) {
        AlertDialog(
            onDismissRequest = { showInvoicesDialog = false },
            icon = {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Settlement Invoices & TDS 📄", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Official statement of earnings and direct bank payouts with UTR numbers:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Payout #CRX-8821", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("$124.50 USD", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 12.sp)
                            }
                            Text("Date: 10 Aug 2026 • UTR: HDFC992817265", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("TDS Deducted (1% Section 194J): $1.24 USD", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Status: Dispatched & Settled to Bank", fontSize = 10.sp, color = Color(0xFF15803D), fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Payout #CRX-7419", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("$105.00 USD", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 12.sp)
                            }
                            Text("Date: 05 Jul 2026 • UTR: SBIN002938471", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("TDS Deducted (1% Section 194J): $1.05 USD", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Status: Dispatched & Settled to Bank", fontSize = 10.sp, color = Color(0xFF15803D), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    Toast.makeText(context, "Downloading PDF Statements to Device...", Toast.LENGTH_SHORT).show()
                    showInvoicesDialog = false
                }) {
                    Text("Download PDF Receipts 📥")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInvoicesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showPayoutPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPayoutPolicyDialog = false },
            icon = {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Monetization & Legal Terms ⚖️", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("1. Age Requirement: Must be 18+ years or represented by a legal guardian with valid PAN card.", fontSize = 11.sp)
                    Text("2. Payout Rate: Standard rate is $1.00 USD per 100,000 valid views across organic Reels & Posts.", fontSize = 11.sp)
                    Text("3. Minimum Withdrawal: Threshold is $100.00. Unmet amounts roll over to the next billing cycle.", fontSize = 11.sp)
                    Text("4. Anti-Fraud Audit: All payout requests undergo automated IP/device telemetry and manual Admin verification before disbursement.", fontSize = 11.sp)
                    Text("5. Right to Withhold: Crexa reserves the right to forfeit balances generated via click-farms, emulators, or bot script attacks.", fontSize = 11.sp, color = Color(0xFF991B1B))
                }
            },
            confirmButton = {
                Button(onClick = { showPayoutPolicyDialog = false }) {
                    Text("I Agree & Understand")
                }
            }
        )
    }

    if (showAdminPayoutDashboard) {
        AlertDialog(
            onDismissRequest = { showAdminPayoutDashboard = false },
            icon = {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(36.dp))
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Admin Payout Management 👑", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Review pending withdrawal requests & execute bank settlements", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Summary Banner
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Pending Requests", fontSize = 11.sp, color = Color(0xFF1E40AF))
                                Text("2 Creators ($229.50)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1D4ED8))
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDBEAFE)) {
                                Text("Rate: $1/100K Views", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }

                    Text("Pending Payout Queue", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    // Request 1
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("@aarav_vlogs (Aarav Sharma)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("12.4M Views • 14.5K Followers", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Badge(containerColor = Color(0xFF10B981)) {
                                    Text("$124.50 (₹10,395)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(4.dp))
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Text("🏦 Bank: HDFC Bank", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("A/C: 50100492817265 • IFSC: HDFC0001234", fontSize = 11.sp)
                            Text("UPI ID: aarav@okhdfcbank", fontSize = 11.sp)
                            Text("PAN Card: ABCPS1234F (Verified ✅)", fontSize = 11.sp, color = Color(0xFF15803D))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "Copied Aarav's Bank Details & UPI ID!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Bank", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Marked as Paid! Settlement UTR recorded.", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1.3f).height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark Paid (UTR)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Request 2
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("@priya_creatives (Priya Patel)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("10.5M Views • 8.2K Followers", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Badge(containerColor = Color(0xFF10B981)) {
                                    Text("$105.00 (₹8,767)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(4.dp))
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Text("🏦 Bank: State Bank of India (SBI)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("A/C: 38291048271 • IFSC: SBIN0004521", fontSize = 11.sp)
                            Text("UPI ID: priya@sbi", fontSize = 11.sp)
                            Text("PAN Card: BNZPP4567K (Verified ✅)", fontSize = 11.sp, color = Color(0xFF15803D))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "Copied Priya's Bank Details & UPI ID!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Bank", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Marked as Paid! Settlement UTR recorded.", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1.3f).height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark Paid (UTR)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAdminPayoutDashboard = false }) {
                    Text("Done")
                }
            }
        )
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report an Issue") },
            text = {
                OutlinedTextField(
                    value = reportText,
                    onValueChange = { reportText = it },
                    placeholder = { Text("Describe the problem or bug...") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReportDialog = false
                        reportText = ""
                        Toast.makeText(context, "Thank you for reporting. We will review it shortly.", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
