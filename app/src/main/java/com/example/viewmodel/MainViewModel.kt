package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.*
import com.example.data.repository.CrexaRepository
import com.example.ui.Screen
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CrexaUiState(
    val currentUser: UserEntity? = null,
    val currentScreen: Screen = Screen.Splash,
    val navigationHistory: List<Screen> = listOf(Screen.Splash),
    val isDarkMode: Boolean? = null,
    val isRefreshing: Boolean = false,
    val users: List<UserEntity> = emptyList(),
    val posts: List<PostEntity> = emptyList(),
    val reels: List<ReelEntity> = emptyList(),
    val stories: List<StoryEntity> = emptyList(),
    val comments: List<CommentEntity> = emptyList(),
    val messages: List<MessageEntity> = emptyList(),
    val notifications: List<NotificationEntity> = emptyList(),
    val userPosts: List<PostEntity> = emptyList(),
    val userReels: List<ReelEntity> = emptyList(),
    val savedPosts: List<PostEntity> = emptyList()
)

typealias LuminaUiState = CrexaUiState

class MainViewModel(private val repository: CrexaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CrexaUiState())
    val uiState: StateFlow<CrexaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Observe current user
            launch {
                repository.getCurrentUserFlow().collect { user ->
                    _uiState.update { it.copy(currentUser = user) }
                    updateDerivedUserContent()
                }
            }

            // Observe all users
            launch {
                repository.getAllUsersFlow().collect { users ->
                    _uiState.update { it.copy(users = users) }
                    updateDerivedUserContent()
                }
            }

            // Observe posts
            launch {
                repository.getAllPosts().collect { posts ->
                    _uiState.update { it.copy(posts = posts) }
                    updateDerivedUserContent()
                }
            }

            // Observe reels
            launch {
                repository.getAllReels().collect { reels ->
                    _uiState.update { it.copy(reels = reels) }
                    updateDerivedUserContent()
                }
            }

            // Observe stories
            launch {
                repository.getAllStories().collect { stories ->
                    _uiState.update { it.copy(stories = stories) }
                }
            }

            // Observe messages
            launch {
                repository.getAllMessages().collect { messages ->
                    _uiState.update { it.copy(messages = messages) }
                }
            }

            // Observe notifications
            launch {
                repository.getAllNotifications().collect { notifications ->
                    _uiState.update { it.copy(notifications = notifications) }
                }
            }

            // Observe dark mode
            launch {
                repository.isDarkMode.collect { isDark ->
                    _uiState.update { it.copy(isDarkMode = isDark) }
                }
            }
        }
    }

    private fun updateDerivedUserContent() {
        val currentState = _uiState.value
        val me = currentState.currentUser ?: return

        val myPosts = currentState.posts.filter { it.userId == me.id }
        val myReels = currentState.reels.filter { it.userId == me.id }
        val saved = currentState.posts.filter { it.isSaved }

        _uiState.update {
            it.copy(
                userPosts = myPosts,
                userReels = myReels,
                savedPosts = saved
            )
        }
    }

    fun navigateTo(screen: Screen) {
        _uiState.update { state ->
            val history = state.navigationHistory.toMutableList()
            if (history.lastOrNull() != screen) {
                history.add(screen)
            }
            state.copy(
                currentScreen = screen,
                navigationHistory = history
            )
        }
    }

    fun navigateBack() {
        _uiState.update { state ->
            val history = state.navigationHistory.toMutableList()
            if (history.size > 1) {
                history.removeAt(history.size - 1)
                val prevScreen = history.last()
                state.copy(
                    currentScreen = prevScreen,
                    navigationHistory = history
                )
            } else {
                state
            }
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            kotlinx.coroutines.delay(1200)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun setDarkMode(isDark: Boolean?) {
        repository.setThemeMode(isDark)
    }

    fun loginWithPassword(usernameOrEmail: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.loginWithCredentials(usernameOrEmail, password)
            if (result.isSuccess) {
                onResult(true, null)
                navigateTo(Screen.Home)
            } else {
                onResult(false, result.exceptionOrNull()?.localizedMessage ?: "Invalid credentials")
            }
        }
    }

    fun registerWithPassword(username: String, email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.registerUser(username, email, password)
            if (result.isSuccess) {
                onResult(true, null)
                navigateTo(Screen.Home)
            } else {
                onResult(false, result.exceptionOrNull()?.localizedMessage ?: "Registration failed")
            }
        }
    }

    fun login(username: String) {
        viewModelScope.launch {
            repository.loginUser(username)
            navigateTo(Screen.Home)
        }
    }

    fun signup(username: String) {
        login(username)
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.delete()
            } catch (e: Throwable) {
                // Ignore if not in Firebase
            }
            repository.deleteCurrentAccount()
            navigateTo(Screen.Login)
            onComplete()
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            } catch (e: Throwable) {
                // Ignore if Firebase not initialized
            }
            repository.logoutUser()
            navigateTo(Screen.Login)
        }
    }

    fun openUserProfile(userId: String) {
        if (userId == _uiState.value.currentUser?.id) {
            navigateTo(Screen.Profile)
        } else {
            navigateTo(Screen.UserProfile(userId))
        }
    }

    fun openComments(postId: String) {
        viewModelScope.launch {
            repository.getCommentsForPost(postId).collect { postComments ->
                _uiState.update { it.copy(comments = postComments) }
            }
        }
        navigateTo(Screen.Comments(postId))
    }

    fun openDirectChat(userId: String) {
        navigateTo(Screen.DirectChat(userId))
    }

    fun openStoryViewer(index: Int) {
        navigateTo(Screen.StoryViewer(index))
    }

    fun toggleLikePost(post: PostEntity) {
        viewModelScope.launch {
            repository.togglePostLike(post.id, post.isLiked)
        }
    }

    fun toggleSavePost(post: PostEntity) {
        viewModelScope.launch {
            repository.togglePostSave(post.id, post.isSaved)
        }
    }

    fun toggleLikeReel(reel: ReelEntity) {
        viewModelScope.launch {
            repository.toggleReelLike(reel.id, reel.isLiked)
        }
    }

    fun toggleSaveReel(reel: ReelEntity) {
        viewModelScope.launch {
            repository.toggleReelSave(reel.id, reel.isSaved)
        }
    }

    fun toggleFollowUser(userId: String) {
        viewModelScope.launch {
            repository.toggleFollowUser(userId)
        }
    }

    fun addComment(postId: String, text: String) {
        viewModelScope.launch {
            repository.addComment(postId = postId, reelId = null, text = text)
        }
    }

    fun toggleLikeComment(comment: CommentEntity) {
        viewModelScope.launch {
            repository.toggleCommentLike(comment.id, comment.isLiked)
        }
    }

    fun sendMessage(receiverUserId: String, text: String) {
        viewModelScope.launch {
            repository.sendMessage(receiverUserId = receiverUserId, text = text)
        }
    }

    fun createPost(mediaUrl: String, caption: String, location: String, hashtags: String, filter: String) {
        viewModelScope.launch {
            repository.createPost(
                mediaUrl = mediaUrl,
                caption = caption,
                location = location,
                hashtags = hashtags,
                filterName = filter
            )
            navigateTo(Screen.Home)
        }
    }

    fun createReel(videoUrl: String, caption: String, audioTitle: String) {
        viewModelScope.launch {
            repository.createReel(
                videoUrl = videoUrl,
                caption = caption,
                audioTitle = audioTitle
            )
            navigateTo(Screen.Reels)
        }
    }

    fun createStory(mediaUrl: String, caption: String) {
        viewModelScope.launch {
            repository.createStory(
                mediaUrl = mediaUrl,
                caption = caption
            )
            navigateTo(Screen.Home)
        }
    }

    fun updateProfile(updatedUser: UserEntity) {
        viewModelScope.launch {
            repository.updateProfile(
                fullName = updatedUser.fullName,
                bio = updatedUser.bio,
                website = updatedUser.website,
                avatarUrl = updatedUser.avatarUrl
            )
            navigateBack()
        }
    }

    fun reportOrBlockUser(action: String, userId: String) {
        viewModelScope.launch {
            if (action == "BLOCK") {
                repository.toggleBlockUser(userId, true)
            } else if (action == "MUTE") {
                repository.toggleMuteUser(userId, true)
            }
        }
    }
}

class MainViewModelFactory(private val repository: CrexaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
