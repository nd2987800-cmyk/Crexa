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
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var isPrivateAccount by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportText by remember { mutableStateOf("") }

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
            // Appearance section
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
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
                supportingContent = { Text("Version 1.0.0 • Crexa Social Platform") },
                leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) }
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
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
        }
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
