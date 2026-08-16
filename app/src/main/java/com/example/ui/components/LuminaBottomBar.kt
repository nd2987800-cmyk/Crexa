package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.Screen
import com.example.ui.theme.CrexaPurple

@Composable
fun CrexaBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    unreadNotifications: Boolean = false
) {
    Surface(
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column {
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            NavigationBar(
                containerColor = Color.White,
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
                                    tint = if (selected) CrexaPurple else Color(0xFF64748B)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) CrexaPurple else Color(0xFF64748B)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFF5F3FF),
                            selectedIconColor = CrexaPurple,
                            unselectedIconColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
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

