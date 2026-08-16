package com.example.data.repository

import com.example.data.SampleData
import com.example.data.local.CrexaDatabase
import com.example.data.local.entities.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CrexaRepository(private val database: CrexaDatabase) {

    private val userDao = database.userDao()
    private val postDao = database.postDao()
    private val storyDao = database.storyDao()
    private val reelDao = database.reelDao()
    private val commentDao = database.commentDao()
    private val notificationDao = database.notificationDao()
    private val messageDao = database.messageDao()

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isDarkMode = MutableStateFlow<Boolean?>(null) // null = system default
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
            syncPostsWithFirestore()
        }
    }

    private suspend fun syncPostsWithFirestore() {
        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("posts")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    CoroutineScope(Dispatchers.IO).launch {
                        val firestorePosts = snapshot.documents.mapNotNull { doc ->
                            val id = doc.id
                            val userId = doc.getString("userId") ?: "user_me"
                            val mediaUrl = doc.getString("mediaUrl") ?: ""
                            val caption = doc.getString("caption") ?: ""
                            val hashtags = doc.getString("hashtags") ?: ""
                            val location = doc.getString("location") ?: ""
                            val filterName = doc.getString("filterName") ?: "Normal"
                            val likesCount = doc.getLong("likesCount")?.toInt() ?: 0
                            val commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                            PostEntity(
                                id = id,
                                userId = userId,
                                mediaUrl = mediaUrl,
                                caption = caption,
                                hashtags = hashtags,
                                location = location,
                                filterName = filterName,
                                likesCount = likesCount,
                                commentsCount = commentsCount,
                                timestamp = timestamp
                            )
                        }
                        if (firestorePosts.isNotEmpty()) {
                            postDao.insertPosts(firestorePosts)
                        }
                    }
                }
        } catch (e: Throwable) {
            // Firestore sync fallback if unconfigured
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        val existingUsers = userDao.getCurrentUser()
        if (existingUsers == null) {
            userDao.insertUsers(SampleData.sampleUsers)
            postDao.insertPosts(SampleData.samplePosts)
            storyDao.insertStories(SampleData.sampleStories)
            reelDao.insertReels(SampleData.sampleReels)
            commentDao.insertComments(SampleData.sampleComments)
            notificationDao.insertNotifications(SampleData.sampleNotifications)
            messageDao.insertMessages(SampleData.sampleMessages)
        }
    }

    // Auth & User
    fun getCurrentUserFlow(): Flow<UserEntity?> = userDao.getCurrentUserFlow()
    fun getUserByIdFlow(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)
    fun getAllUsersFlow(): Flow<List<UserEntity>> = userDao.getAllUsers()
    fun searchUsers(query: String): Flow<List<UserEntity>> = userDao.searchUsers(query)

    suspend fun loginUser(username: String) = withContext(Dispatchers.IO) {
        val user = userDao.getCurrentUser()
        if (user != null) {
            userDao.updateUser(user.copy(username = username))
        } else {
            val newUser = UserEntity(
                id = "user_me",
                username = username,
                fullName = username.replaceFirstChar { it.uppercase() },
                bio = "Welcome to my Crexa profile ✨",
                avatarUrl = "android.resource://com.aistudio.lumina.social/drawable/img_crexa_brand_logo_1786179516858",
                isCurrentUser = true
            )
            userDao.insertUser(newUser)
        }
        _isLoggedIn.value = true
    }

    suspend fun logoutUser() = withContext(Dispatchers.IO) {
        _isLoggedIn.value = false
    }

    suspend fun updateProfile(fullName: String, bio: String, website: String, avatarUrl: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser()
        if (currentUser != null) {
            val updated = currentUser.copy(
                fullName = fullName,
                bio = bio,
                website = website,
                avatarUrl = if (avatarUrl.isNotBlank()) avatarUrl else currentUser.avatarUrl
            )
            userDao.updateUser(updated)
        }
    }

    suspend fun toggleFollowUser(userId: String) = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId)
        if (user != null) {
            val newFollowState = !user.isFollowing
            userDao.updateFollowStatus(userId, newFollowState)
            if (newFollowState) {
                // Add notification
                val current = userDao.getCurrentUser()
                notificationDao.insertNotification(
                    NotificationEntity(
                        id = "notif_${System.currentTimeMillis()}",
                        recipientUserId = userId,
                        actorUserId = current?.id ?: "user_me",
                        type = "FOLLOW",
                        message = "started following you.",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun toggleMuteUser(userId: String, isMuted: Boolean) = withContext(Dispatchers.IO) {
        userDao.updateMuteStatus(userId, isMuted)
    }

    suspend fun toggleBlockUser(userId: String, isBlocked: Boolean) = withContext(Dispatchers.IO) {
        userDao.updateBlockStatus(userId, isBlocked)
    }

    // Posts
    fun getAllPosts(): Flow<List<PostEntity>> = postDao.getAllPosts()
    fun getPostsByUser(userId: String): Flow<List<PostEntity>> = postDao.getPostsByUser(userId)
    fun getSavedPosts(): Flow<List<PostEntity>> = postDao.getSavedPosts()
    fun searchPosts(query: String): Flow<List<PostEntity>> = postDao.searchPosts(query)

    suspend fun createPost(mediaUrl: String, caption: String, location: String, hashtags: String, filterName: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser() ?: return@withContext
        val newPost = PostEntity(
            id = "post_${System.currentTimeMillis()}",
            userId = currentUser.id,
            mediaUrl = mediaUrl,
            caption = caption,
            location = location,
            hashtags = hashtags,
            filterName = filterName,
            timestamp = System.currentTimeMillis()
        )
        postDao.insertPost(newPost)
        userDao.updateUser(currentUser.copy(postsCount = currentUser.postsCount + 1))

        // Sync to Firestore
        try {
            val firestoreMap = hashMapOf(
                "userId" to newPost.userId,
                "mediaUrl" to newPost.mediaUrl,
                "caption" to newPost.caption,
                "location" to newPost.location,
                "hashtags" to newPost.hashtags,
                "filterName" to newPost.filterName,
                "likesCount" to newPost.likesCount,
                "commentsCount" to newPost.commentsCount,
                "timestamp" to newPost.timestamp
            )
            FirebaseFirestore.getInstance()
                .collection("posts")
                .document(newPost.id)
                .set(firestoreMap)
        } catch (e: Throwable) {
            // Local Room cache holds post if Firestore network/config is unavailable
        }
    }

    suspend fun togglePostLike(postId: String, currentLiked: Boolean) = withContext(Dispatchers.IO) {
        postDao.togglePostLike(postId, !currentLiked)
    }

    suspend fun togglePostSave(postId: String, currentSaved: Boolean) = withContext(Dispatchers.IO) {
        postDao.togglePostSave(postId, !currentSaved)
    }

    suspend fun deletePost(postId: String) = withContext(Dispatchers.IO) {
        postDao.deletePost(postId)
    }

    // Stories
    fun getAllStories(): Flow<List<StoryEntity>> = storyDao.getAllStories()

    suspend fun createStory(mediaUrl: String, caption: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser() ?: return@withContext
        val newStory = StoryEntity(
            id = "story_${System.currentTimeMillis()}",
            userId = currentUser.id,
            mediaUrl = mediaUrl,
            caption = caption,
            timestamp = System.currentTimeMillis()
        )
        storyDao.insertStory(newStory)
    }

    suspend fun markStorySeen(storyId: String) = withContext(Dispatchers.IO) {
        storyDao.markStorySeen(storyId)
    }

    // Reels
    fun getAllReels(): Flow<List<ReelEntity>> = reelDao.getAllReels()
    fun getReelsByUser(userId: String): Flow<List<ReelEntity>> = reelDao.getReelsByUser(userId)
    fun getSavedReels(): Flow<List<ReelEntity>> = reelDao.getSavedReels()

    suspend fun createReel(videoUrl: String, caption: String, audioTitle: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser() ?: return@withContext
        val newReel = ReelEntity(
            id = "reel_${System.currentTimeMillis()}",
            userId = currentUser.id,
            videoUrl = videoUrl,
            thumbnailUrl = videoUrl,
            caption = caption,
            audioTitle = if (audioTitle.isBlank()) "Original Audio" else audioTitle,
            audioArtist = currentUser.fullName,
            timestamp = System.currentTimeMillis()
        )
        reelDao.insertReel(newReel)
    }

    suspend fun toggleReelLike(reelId: String, currentLiked: Boolean) = withContext(Dispatchers.IO) {
        reelDao.toggleReelLike(reelId, !currentLiked)
    }

    suspend fun toggleReelSave(reelId: String, currentSaved: Boolean) = withContext(Dispatchers.IO) {
        reelDao.toggleReelSave(reelId, !currentSaved)
    }

    // Comments
    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>> = commentDao.getCommentsForPost(postId)
    fun getCommentsForReel(reelId: String): Flow<List<CommentEntity>> = commentDao.getCommentsForReel(reelId)

    suspend fun addComment(postId: String?, reelId: String?, text: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser() ?: return@withContext
        val newComment = CommentEntity(
            id = "comment_${System.currentTimeMillis()}",
            postId = postId,
            reelId = reelId,
            userId = currentUser.id,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        commentDao.insertComment(newComment)
        if (postId != null) postDao.incrementCommentsCount(postId)
        if (reelId != null) reelDao.incrementReelComments(reelId)
    }

    suspend fun toggleCommentLike(commentId: String, currentLiked: Boolean) = withContext(Dispatchers.IO) {
        commentDao.toggleCommentLike(commentId, !currentLiked)
    }

    // Notifications
    fun getAllNotifications(): Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    // Messages
    fun getMessagesBetweenUsers(userId1: String, userId2: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesBetweenUsers(userId1, userId2)

    fun getAllMessages(): Flow<List<MessageEntity>> = messageDao.getAllMessages()

    suspend fun sendMessage(receiverUserId: String, text: String, mediaUrl: String? = null) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser() ?: return@withContext
        val newMessage = MessageEntity(
            id = "msg_${System.currentTimeMillis()}",
            senderUserId = currentUser.id,
            receiverUserId = receiverUserId,
            text = text,
            mediaUrl = mediaUrl,
            timestamp = System.currentTimeMillis()
        )
        messageDao.insertMessage(newMessage)
    }

    fun setThemeMode(isDark: Boolean?) {
        _isDarkMode.value = isDark
    }
}
