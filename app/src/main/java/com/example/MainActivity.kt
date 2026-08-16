package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.CrexaDatabase
import com.example.data.repository.CrexaRepository
import com.example.ui.Screen
import com.example.ui.components.CrexaBottomBar
import com.example.ui.screens.*
import com.example.ui.theme.CrexaTheme
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = CrexaDatabase.getDatabase(applicationContext)
        val repository = CrexaRepository(database)
        val factory = MainViewModelFactory(repository)

        setContent {
            val viewModel: MainViewModel = viewModel(factory = factory)
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            CrexaTheme(forceDarkTheme = state.isDarkMode) {
                if (state.currentScreen == Screen.Splash) {
                    SplashScreen(
                        onSplashFinished = {
                            if (state.currentUser != null) {
                                viewModel.navigateTo(Screen.Home)
                            } else {
                                viewModel.navigateTo(Screen.Login)
                            }
                        }
                    )
                } else if (state.currentScreen == Screen.Login) {
                    LoginScreen(
                        onLoginSuccess = { usernameOrEmail, password ->
                            viewModel.loginWithPassword(usernameOrEmail, password) { success, _ ->
                                if (!success) {
                                    viewModel.login(usernameOrEmail)
                                }
                            }
                        },
                        onNavigateToSignUp = {
                            viewModel.navigateTo(Screen.SignUp)
                        }
                    )
                } else if (state.currentScreen == Screen.SignUp) {
                    SignUpScreen(
                        onSignUpSuccess = { username, email, password ->
                            viewModel.registerWithPassword(username, email, password) { success, _ ->
                                if (!success) {
                                    viewModel.signup(username)
                                }
                            }
                        },
                        onNavigateToLogin = {
                            viewModel.navigateTo(Screen.Login)
                        }
                    )
                } else {
                    val showBottomBar = when (state.currentScreen) {
                        Screen.Home, Screen.Reels, Screen.Create, Screen.Notifications, Screen.Profile -> true
                        else -> false
                    }

                    BackHandler(enabled = state.navigationHistory.size > 1) {
                        viewModel.navigateBack()
                    }

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                CrexaBottomBar(
                                    currentScreen = state.currentScreen,
                                    onNavigate = { targetScreen ->
                                        viewModel.navigateTo(targetScreen)
                                    },
                                    unreadNotifications = state.notifications.any { !it.isRead }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (val screen = state.currentScreen) {
                                Screen.Home -> {
                                    HomeScreen(
                                        posts = state.posts,
                                        stories = state.stories,
                                        users = state.users,
                                        currentUser = state.currentUser,
                                        isRefreshing = state.isRefreshing,
                                        onRefresh = { viewModel.refreshFeed() },
                                        onOpenSearch = { viewModel.navigateTo(Screen.Search) },
                                        onOpenMessages = { viewModel.navigateTo(Screen.Messages) },
                                        onOpenCreate = { viewModel.navigateTo(Screen.Create) },
                                        onOpenAiStudio = { viewModel.navigateTo(Screen.AiStudio) },
                                        onOpenStory = { idx -> viewModel.openStoryViewer(idx) },
                                        onUserClick = { userId -> viewModel.openUserProfile(userId) },
                                        onLikePost = { post -> viewModel.toggleLikePost(post) },
                                        onCommentPost = { postId -> viewModel.openComments(postId) },
                                        onSavePost = { post -> viewModel.toggleSavePost(post) },
                                        onReportBlockUser = { action, userId -> viewModel.reportOrBlockUser(action, userId) }
                                    )
                                }
                                Screen.AiStudio -> {
                                    AiStudioScreen(
                                        onBackClick = { viewModel.navigateBack() },
                                        onCreatePostFromMedia = { mediaUrl, caption, hashtags ->
                                            viewModel.createPost(mediaUrl, caption, "Lumina AI Studio", hashtags, "Normal")
                                            viewModel.navigateTo(Screen.Home)
                                        },
                                        onCreateReelFromMedia = { videoUrl, caption ->
                                            viewModel.createReel(videoUrl, caption, "AI Generated Audio")
                                            viewModel.navigateTo(Screen.Reels)
                                        }
                                    )
                                }
                                Screen.Reels -> {
                                    ReelsScreen(
                                        reels = state.reels,
                                        users = state.users,
                                        onUserClick = { userId -> viewModel.openUserProfile(userId) },
                                        onLikeReel = { reel -> viewModel.toggleLikeReel(reel) },
                                        onCommentReel = { reelId -> viewModel.openComments(reelId) },
                                        onSaveReel = { reel -> viewModel.toggleSaveReel(reel) },
                                        onFollowUser = { userId -> viewModel.toggleFollowUser(userId) }
                                    )
                                }
                                Screen.Search -> {
                                    SearchScreen(
                                        posts = state.posts,
                                        users = state.users,
                                        onUserClick = { userId -> viewModel.openUserProfile(userId) },
                                        onPostClick = { postId -> viewModel.openComments(postId) }
                                    )
                                }
                                Screen.Create -> {
                                    CreatePostScreen(
                                        onCreatePost = { mediaUrl, caption, location, hashtags, filter ->
                                            viewModel.createPost(mediaUrl, caption, location, hashtags, filter)
                                        },
                                        onCreateReel = { videoUrl, caption, audioTitle ->
                                            viewModel.createReel(videoUrl, caption, audioTitle)
                                        },
                                        onCreateStory = { mediaUrl, caption ->
                                            viewModel.createStory(mediaUrl, caption)
                                        },
                                        onBackClick = { viewModel.navigateBack() },
                                        onOpenFullCamera = { viewModel.navigateTo(Screen.Camera) },
                                        onOpenAiStudio = { viewModel.navigateTo(Screen.AiStudio) }
                                    )
                                }
                                Screen.Camera -> {
                                    CameraScreen(
                                        onMediaCaptured = { mediaUri, isVideo, mode ->
                                            when (mode) {
                                                CameraCaptureMode.VIDEO -> {
                                                    viewModel.createReel(mediaUri, "Captured from Crexa CameraX", "Original Audio")
                                                }
                                                CameraCaptureMode.STORY -> {
                                                    viewModel.createStory(mediaUri, "Story from Camera")
                                                }
                                                CameraCaptureMode.LIVE -> {
                                                    viewModel.createPost(mediaUri, "Live Broadcast Replay ✨", "Crexa Live Studio", "#live #crexa", "Normal")
                                                }
                                                CameraCaptureMode.POST -> {
                                                    viewModel.createPost(mediaUri, "Captured with Crexa Camera", "Crexa Studio", "#photography #crexa", "Normal")
                                                }
                                            }
                                        },
                                        onCloseClick = { viewModel.navigateBack() }
                                    )
                                }
                                Screen.Notifications -> {
                                    NotificationsScreen(
                                        notifications = state.notifications,
                                        users = state.users,
                                        posts = state.posts,
                                        onUserClick = { userId -> viewModel.openUserProfile(userId) },
                                        onFollowBack = { userId -> viewModel.toggleFollowUser(userId) },
                                        onPostClick = { postId -> viewModel.openComments(postId) }
                                    )
                                }
                                Screen.Profile -> {
                                    ProfileScreen(
                                        user = state.currentUser,
                                        isCurrentUser = true,
                                        userPosts = state.userPosts,
                                        userReels = state.userReels,
                                        savedPosts = state.savedPosts,
                                        onEditProfileClick = { viewModel.navigateTo(Screen.EditProfile) },
                                        onSettingsClick = { viewModel.navigateTo(Screen.Settings) },
                                        onFollowClick = {},
                                        onMessageClick = {},
                                        onPostClick = { postId -> viewModel.openComments(postId) }
                                    )
                                }
                                is Screen.UserProfile -> {
                                    val targetUser = state.users.find { it.id == screen.userId }
                                    val isMe = targetUser?.id == state.currentUser?.id
                                    val targetPosts = state.posts.filter { it.userId == screen.userId }
                                    val targetReels = state.reels.filter { it.userId == screen.userId }

                                    ProfileScreen(
                                        user = targetUser,
                                        isCurrentUser = isMe,
                                        userPosts = targetPosts,
                                        userReels = targetReels,
                                        savedPosts = emptyList(),
                                        onEditProfileClick = { viewModel.navigateTo(Screen.EditProfile) },
                                        onSettingsClick = { viewModel.navigateTo(Screen.Settings) },
                                        onFollowClick = { targetUser?.let { viewModel.toggleFollowUser(it.id) } },
                                        onMessageClick = { targetUser?.let { viewModel.openDirectChat(it.id) } },
                                        onPostClick = { postId -> viewModel.openComments(postId) }
                                    )
                                }
                                Screen.EditProfile -> {
                                    EditProfileScreen(
                                        currentUser = state.currentUser,
                                        onSaveProfile = { fullName, bio, website, avatarUrl ->
                                            state.currentUser?.let { me ->
                                                val updated = me.copy(fullName = fullName, bio = bio, website = website, avatarUrl = avatarUrl)
                                                viewModel.updateProfile(updated)
                                            }
                                        },
                                        onBackClick = { viewModel.navigateBack() }
                                    )
                                }
                                Screen.Settings -> {
                                    SettingsScreen(
                                        currentDarkMode = state.isDarkMode,
                                        onSetThemeMode = { isDark -> viewModel.setDarkMode(isDark) },
                                        onLogout = { viewModel.logout() },
                                        onDeleteAccount = { viewModel.deleteAccount {} },
                                        onBackClick = { viewModel.navigateBack() }
                                    )
                                }
                                Screen.Messages -> {
                                    MessagesScreen(
                                        users = state.users,
                                        currentUser = state.currentUser,
                                        onOpenChat = { userId -> viewModel.openDirectChat(userId) },
                                        onBackClick = { viewModel.navigateBack() }
                                    )
                                }
                                is Screen.DirectChat -> {
                                    val recipient = state.users.find { it.id == screen.userId }
                                    val chatMessages = state.messages.filter {
                                        (it.senderUserId == state.currentUser?.id && it.receiverUserId == screen.userId) ||
                                                (it.senderUserId == screen.userId && it.receiverUserId == state.currentUser?.id)
                                    }

                                    DirectChatScreen(
                                        recipientUser = recipient,
                                        messages = chatMessages,
                                        currentUser = state.currentUser,
                                        onSendMessage = { text ->
                                            viewModel.sendMessage(screen.userId, text)
                                        },
                                        onBackClick = { viewModel.navigateBack() }
                                    )
                                }
                                is Screen.Comments -> {
                                    val postComments = state.comments.filter { it.postId == screen.postId }

                                    CommentsScreen(
                                        comments = postComments,
                                        users = state.users,
                                        onAddComment = { text -> viewModel.addComment(screen.postId, text) },
                                        onLikeComment = { comment -> viewModel.toggleLikeComment(comment) },
                                        onBackClick = { viewModel.navigateBack() }
                                    )
                                }
                                is Screen.StoryViewer -> {
                                    StoryViewerScreen(
                                        stories = state.stories,
                                        users = state.users,
                                        initialIndex = screen.index,
                                        onClose = { viewModel.navigateBack() },
                                        onSendReply = { recipientId, msg -> viewModel.sendMessage(recipientId, msg) }
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}

