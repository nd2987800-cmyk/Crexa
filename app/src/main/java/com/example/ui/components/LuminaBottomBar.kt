package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.Screen

@Composable
fun CrexaBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    unreadNotifications: Boolean = false
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            val items = listOf(
                NavigationItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
                NavigationItem(Screen.Reels, "Reels", Icons.Filled.Movie, Icons.Outlined.Movie, "nav_reels"),
                NavigationItem(Screen.Create, "Create", Icons.Filled.AddCircle, Icons.Outlined.AddCircle, "nav_create"),
                NavigationItem(Screen.Notifications, "Activity", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "nav_notifications"),
                NavigationItem(Screen.Profile, "Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
            )

            items.forEach { item ->
                val selected = currentScreen.route == item.screen.route
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.screen) },
                    icon = {
                        BadgedBox(badge = {
                            if (item.screen == Screen.Notifications && unreadNotifications) {
                                Badge()
                            }
                        }) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag(item.testTag)
                )
            }
        }
    }
}

@Composable
fun LuminaBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    unreadNotifications: Boolean = false
) = CrexaBottomBar(currentScreen, onNavigate, unreadNotifications)

private data class NavigationItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)
