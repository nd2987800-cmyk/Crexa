package com.example.ui

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Reels : Screen("reels")
    object Search : Screen("search")
    object Create : Screen("create")
    object Camera : Screen("camera")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")
    object Messages : Screen("messages")
    object AiStudio : Screen("ai_studio")
    data class DirectChat(val userId: String) : Screen("direct_chat/$userId")
    data class Comments(val postId: String) : Screen("comments/$postId")
    data class UserProfile(val userId: String) : Screen("user_profile/$userId")
    data class StoryViewer(val index: Int) : Screen("story_viewer/$index")
}
