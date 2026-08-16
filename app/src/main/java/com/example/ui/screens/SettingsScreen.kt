package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.SimpleTopBar

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
    var showReportDialog by remember { mutableStateOf(false) }
    var reportText by remember { mutableStateOf("") }
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    var isSubscribed by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

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
                text = "Privacy & Safety",
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
                        onCheckedChange = { isPrivateAccount = it }
                    )
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
