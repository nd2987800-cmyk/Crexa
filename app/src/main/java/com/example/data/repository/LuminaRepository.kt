package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import java.util.UUID
import com.example.data.firebase.FirebaseManager
import com.example.data.local.CrexaDatabase
import com.example.data.local.entities.*
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CrexaRepository(
    private val context: Context,
    private val database: CrexaDatabase
) {
    private val TAG = "CrexaRepository"

    private val userDao = database.userDao()
    private val postDao = database.postDao()
    private val storyDao = database.storyDao()
    private val reelDao = database.reelDao()
    private val commentDao = database.commentDao()
    private val notificationDao = database.notificationDao()
    private val messageDao = database.messageDao()

    private val _isLoggedIn = MutableStateFlow(FirebaseManager.auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isDarkMode = MutableStateFlow<Boolean?>(null) // null = system default
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    private val listeners = mutableListOf<ListenerRegistration>()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            checkCurrentAuthUser()
            startFirestoreRealtimeSync()
        }
    }

    private suspend fun checkCurrentAuthUser() {
        val currentAuth = FirebaseManager.auth.currentUser
        if (currentAuth != null) {
            val uid = currentAuth.uid
            val profile = FirebaseManager.getUserProfile(uid)
            if (profile != null) {
                userDao.clearCurrentUserFlag()
                userDao.insertUser(profile.copy(isCurrentUser = true))
                _isLoggedIn.value = true
            } else {
                val newProfile = UserEntity(
                    id = uid,
                    username = currentAuth.displayName?.lowercase()?.replace(" ", "_")
                        ?: currentAuth.email?.substringBefore("@")
                        ?: "user_${uid.take(5)}",
                    fullName = currentAuth.displayName ?: "Crexa User",
                    bio = "Welcome to my Crexa profile ✨",
                    avatarUrl = currentAuth.photoUrl?.toString() ?: "",
                    email = currentAuth.email ?: "",
                    isCurrentUser = true
                )
                FirebaseManager.saveUserProfile(newProfile)
                userDao.clearCurrentUserFlag()
                userDao.insertUser(newProfile)
                _isLoggedIn.value = true
            }
        } else {
            val localUser = userDao.getCurrentUser()
            if (localUser != null && !localUser.id.startsWith("user_")) {
                _isLoggedIn.value = true
            } else if (localUser == null) {
                _isLoggedIn.value = false
            }
        }
    }

    private fun startFirestoreRealtimeSync() {
        try {
            // 1. Real-time Posts listener
            val postsListener = FirebaseManager.firestore.collection(FirebaseManager.COL_POSTS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        Log.e(TAG, "Posts sync error: ${error?.message}")
                        return@addSnapshotListener
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        val currentUserId = FirebaseManager.auth.currentUser?.uid ?: userDao.getCurrentUser()?.id
                        val posts = snapshot.documents.mapNotNull { doc ->
                            val id = doc.id
                            val userId = doc.getString("userId") ?: return@mapNotNull null
                            val mediaUrl = doc.getString("mediaUrl") ?: ""
                            val caption = doc.getString("caption") ?: ""
                            val location = doc.getString("location") ?: ""
                            val hashtags = doc.getString("hashtags") ?: ""
                            val filterName = doc.getString("filterName") ?: "Normal"
                            val likesCount = doc.getLong("likesCount")?.toInt() ?: 0
                            val commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                            // Check local like state if available
                            val localPost = postDao.getPostByIdSync(id)
                            PostEntity(
                                id = id,
                                userId = userId,
                                mediaUrl = mediaUrl,
                                caption = caption,
                                location = location,
                                hashtags = hashtags,
                                filterName = filterName,
                                likesCount = likesCount,
                                commentsCount = commentsCount,
                                timestamp = timestamp,
                                isLiked = localPost?.isLiked ?: false,
                                isSaved = localPost?.isSaved ?: false
                            )
                        }
                        if (posts.isNotEmpty()) {
                            postDao.insertPosts(posts)
                        }
                    }
                }
            listeners.add(postsListener)

            // 2. Real-time Reels listener
            val reelsListener = FirebaseManager.firestore.collection(FirebaseManager.COL_REELS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    CoroutineScope(Dispatchers.IO).launch {
                        val reels = snapshot.documents.mapNotNull { doc ->
                            val id = doc.id
                            val userId = doc.getString("userId") ?: return@mapNotNull null
                            val videoUrl = doc.getString("videoUrl") ?: ""
                            val thumbnailUrl = doc.getString("thumbnailUrl") ?: videoUrl
                            val caption = doc.getString("caption") ?: ""
                            val audioTitle = doc.getString("audioTitle") ?: "Original Audio"
                            val audioArtist = doc.getString("audioArtist") ?: ""
                            val likesCount = doc.getLong("likesCount")?.toInt() ?: 0
                            val commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                            ReelEntity(
                                id = id,
                                userId = userId,
                                videoUrl = videoUrl,
                                thumbnailUrl = thumbnailUrl,
                                caption = caption,
                                audioTitle = audioTitle,
                                audioArtist = audioArtist,
                                likesCount = likesCount,
                                commentsCount = commentsCount,
                                timestamp = timestamp
                            )
                        }
                        if (reels.isNotEmpty()) {
                            reelDao.insertReels(reels)
                        }
                    }
                }
            listeners.add(reelsListener)

            // 3. Real-time Stories listener
            val storiesListener = FirebaseManager.firestore.collection(FirebaseManager.COL_STORIES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    CoroutineScope(Dispatchers.IO).launch {
                        val currentUid = FirebaseManager.auth.currentUser?.uid ?: ""
                        val stories = snapshot.documents.mapNotNull { doc ->
                            val id = doc.id
                            val userId = doc.getString("userId") ?: return@mapNotNull null
                            val mediaUrl = doc.getString("mediaUrl") ?: ""
                            val caption = doc.getString("caption") ?: ""
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            @Suppress("UNCHECKED_CAST")
                            val seenList = (doc.get("seenUserIds") as? List<String>) ?: emptyList()

                            StoryEntity(
                                id = id,
                                userId = userId,
                                mediaUrl = mediaUrl,
                                caption = caption,
                                timestamp = timestamp,
                                isSeen = seenList.contains(currentUid)
                            )
                        }
                        if (stories.isNotEmpty()) {
                            storyDao.insertStories(stories)
                        }
                    }
                }
            listeners.add(storiesListener)

            // 4. Real-time Users listener
            val usersListener = FirebaseManager.firestore.collection(FirebaseManager.COL_USERS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    CoroutineScope(Dispatchers.IO).launch {
                        val currentAuthUid = FirebaseManager.auth.currentUser?.uid
                        val users = snapshot.documents.mapNotNull { doc ->
                            val id = doc.id
                            val username = doc.getString("username") ?: "user"
                            val email = doc.getString("email") ?: ""
                            val fullName = doc.getString("fullName") ?: username
                            val bio = doc.getString("bio") ?: ""
                            val website = doc.getString("website") ?: ""
                            val avatarUrl = doc.getString("avatarUrl") ?: ""
                            val followersCount = doc.getLong("followersCount")?.toInt() ?: 0
                            val followingCount = doc.getLong("followingCount")?.toInt() ?: 0
                            val postsCount = doc.getLong("postsCount")?.toInt() ?: 0
                            val isCurrent = (id == currentAuthUid)

                            UserEntity(
                                id = id,
                                username = username,
                                email = email,
                                fullName = fullName,
                                bio = bio,
                                website = website,
                                avatarUrl = avatarUrl,
                                followersCount = followersCount,
                                followingCount = followingCount,
                                postsCount = postsCount,
                                isCurrentUser = isCurrent
                            )
                        }
                        if (users.isNotEmpty()) {
                            userDao.insertUsers(users)
                        }
                    }
                }
            listeners.add(usersListener)

            // 5. Real-time Comments listener
            val commentsListener = FirebaseManager.firestore.collection(FirebaseManager.COL_COMMENTS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    CoroutineScope(Dispatchers.IO).launch {
                        val comments = snapshot.documents.mapNotNull { doc ->
                            val id = doc.id
                            val postId = doc.getString("postId")?.takeIf { it.isNotEmpty() }
                            val reelId = doc.getString("reelId")?.takeIf { it.isNotEmpty() }
                            val userId = doc.getString("userId") ?: return@mapNotNull null
                            val text = doc.getString("text") ?: ""
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val likesCount = doc.getLong("likesCount")?.toInt() ?: 0

                            CommentEntity(
                                id = id,
                                postId = postId,
                                reelId = reelId,
                                userId = userId,
                                text = text,
                                likesCount = likesCount,
                                timestamp = timestamp
                            )
                        }
                        if (comments.isNotEmpty()) {
                            commentDao.insertComments(comments)
                        }
                    }
                }
            listeners.add(commentsListener)

            // 6. Real-time Messages listener
            val messagesListener = FirebaseManager.firestore.collection(FirebaseManager.COL_MESSAGES)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    CoroutineScope(Dispatchers.IO).launch {
                        val msgs = snapshot.documents.mapNotNull { doc ->
                            val id = doc.id
                            val senderUserId = doc.getString("senderUserId") ?: return@mapNotNull null
                            val receiverUserId = doc.getString("receiverUserId") ?: return@mapNotNull null
                            val text = doc.getString("text") ?: ""
                            val mediaUrl = doc.getString("mediaUrl")?.takeIf { it.isNotEmpty() }
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val isRead = doc.getBoolean("isRead") ?: false

                            MessageEntity(
                                id = id,
                                senderUserId = senderUserId,
                                receiverUserId = receiverUserId,
                                text = text,
                                mediaUrl = mediaUrl,
                                timestamp = timestamp,
                                isRead = isRead
                            )
                        }
                        if (msgs.isNotEmpty()) {
                            messageDao.insertMessages(msgs)
                        }
                    }
                }
            listeners.add(messagesListener)

            // 7. Real-time Notifications listener
            val notifsListener = FirebaseManager.firestore.collection(FirebaseManager.COL_NOTIFICATIONS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    CoroutineScope(Dispatchers.IO).launch {
                        val notifs = snapshot.documents.mapNotNull { doc ->
                            val id = doc.id
                            val recipientUserId = doc.getString("recipientUserId") ?: return@mapNotNull null
                            val actorUserId = doc.getString("actorUserId") ?: ""
                            val type = doc.getString("type") ?: "GENERAL"
                            val message = doc.getString("message") ?: ""
                            val postOrReelId = doc.getString("postOrReelId")?.takeIf { it.isNotEmpty() }
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val isRead = doc.getBoolean("isRead") ?: false

                            NotificationEntity(
                                id = id,
                                recipientUserId = recipientUserId,
                                actorUserId = actorUserId,
                                type = type,
                                postOrReelId = postOrReelId,
                                message = message,
                                timestamp = timestamp,
                                isRead = isRead
                            )
                        }
                        if (notifs.isNotEmpty()) {
                            notificationDao.insertNotifications(notifs)
                        }
                    }
                }
            listeners.add(notifsListener)

        } catch (e: Throwable) {
            Log.e(TAG, "Failed to start Firestore realtime listeners: ${e.message}")
        }
    }

    // -------------------------------------------------------------
    // Auth & User Operations
    // -------------------------------------------------------------
    fun getCurrentUserFlow(): Flow<UserEntity?> = userDao.getCurrentUserFlow()
    fun getUserByIdFlow(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)
    fun getAllUsersFlow(): Flow<List<UserEntity>> = userDao.getAllUsers()
    fun searchUsers(query: String): Flow<List<UserEntity>> = userDao.searchUsers(query)

    suspend fun searchUsersFromFirestore(query: String): List<UserEntity> = withContext(Dispatchers.IO) {
        val firestoreResults = FirebaseManager.searchUsersByUsername(query)
        if (firestoreResults.isNotEmpty()) {
            userDao.insertUsers(firestoreResults)
        }
        firestoreResults
    }

    suspend fun registerUser(
        username: String,
        email: String,
        password: String,
        phoneNumber: String = ""
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase()
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()
        val cleanPhone = phoneNumber.trim()

        try {
            // Check username in Firestore
            val existingUsernameDoc = FirebaseManager.firestore.collection(FirebaseManager.COL_USERNAMES)
                .document(cleanUsername).get().await()
            if (existingUsernameDoc.exists()) {
                return@withContext Result.failure(Exception("Username @$cleanUsername is already taken!"))
            }

            // Create Firebase Auth user
            val emailToRegister = if (cleanEmail.contains("@")) cleanEmail else "$cleanUsername@crexa.app"
            val authResult = FirebaseManager.auth.createUserWithEmailAndPassword(emailToRegister, cleanPassword).await()
            val authUser = authResult.user ?: throw Exception("Firebase user creation failed")
            val uid = authUser.uid

            // Update displayName in Auth
            val profileChange = UserProfileChangeRequest.Builder()
                .setDisplayName(cleanUsername)
                .build()
            authUser.updateProfile(profileChange).await()

            val newUser = UserEntity(
                id = uid,
                username = cleanUsername,
                fullName = cleanUsername.replaceFirstChar { it.uppercase() },
                bio = "Welcome to my Crexa profile ✨",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                email = emailToRegister,
                phoneNumber = cleanPhone,
                isCurrentUser = true
            )

            // Save to Firestore and Room
            FirebaseManager.saveUserProfile(newUser)
            userDao.clearCurrentUserFlag()
            userDao.insertUser(newUser)
            _isLoggedIn.value = true

            return@withContext Result.success(newUser)
        } catch (e: Throwable) {
            Log.e(TAG, "Registration error: ${e.message}")
            // Fallback to local DB registration if offline/Firebase error
            val localUser = UserEntity(
                id = "user_${System.currentTimeMillis()}",
                username = cleanUsername,
                fullName = cleanUsername.replaceFirstChar { it.uppercase() },
                bio = "Welcome to my Crexa profile ✨",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                email = cleanEmail,
                phoneNumber = cleanPhone,
                isCurrentUser = true
            )
            userDao.clearCurrentUserFlag()
            userDao.insertUser(localUser)
            _isLoggedIn.value = true
            return@withContext Result.success(localUser)
        }
    }

    suspend fun loginWithCredentials(usernameOrEmailOrPhone: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val query = usernameOrEmailOrPhone.trim().lowercase()
        val cleanPassword = password.trim()

        try {
            var emailToUse = query
            if (!query.contains("@")) {
                // Check if query is a phone number or username
                val phoneQuery = FirebaseManager.firestore.collection(FirebaseManager.COL_USERS)
                    .whereEqualTo("phoneNumber", query).limit(1).get().await()
                if (!phoneQuery.isEmpty) {
                    emailToUse = phoneQuery.documents[0].getString("email") ?: "$query@crexa.app"
                } else {
                    // Look up email by username in Firestore
                    val userDoc = FirebaseManager.firestore.collection(FirebaseManager.COL_USERS)
                        .whereEqualTo("username", query).limit(1).get().await()
                    if (!userDoc.isEmpty) {
                        emailToUse = userDoc.documents[0].getString("email") ?: "$query@crexa.app"
                    } else {
                        emailToUse = "$query@crexa.app"
                    }
                }
            }

            val authResult = FirebaseManager.auth.signInWithEmailAndPassword(emailToUse, cleanPassword).await()
            val authUser = authResult.user ?: throw Exception("Login failed: User not found")
            val uid = authUser.uid

            var profile = FirebaseManager.getUserProfile(uid)
            if (profile == null) {
                profile = UserEntity(
                    id = uid,
                    username = query.substringBefore("@"),
                    fullName = query.substringBefore("@").replaceFirstChar { it.uppercase() },
                    bio = "Welcome to my Crexa profile ✨",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                    email = emailToUse,
                    isCurrentUser = true
                )
                FirebaseManager.saveUserProfile(profile)
            }

            userDao.clearCurrentUserFlag()
            userDao.insertUser(profile.copy(isCurrentUser = true))
            _isLoggedIn.value = true

            return@withContext Result.success(profile)
        } catch (e: Throwable) {
            Log.e(TAG, "Login error: ${e.message}")
            // Check local DB for offline user match
            val localUser = userDao.getUserByUsernameOrEmail(query)
            if (localUser != null) {
                userDao.clearCurrentUserFlag()
                userDao.insertUser(localUser.copy(isCurrentUser = true))
                _isLoggedIn.value = true
                return@withContext Result.success(localUser)
            }
            return@withContext Result.failure(e)
        }
    }

    suspend fun loginWithPhoneOtp(phoneNumber: String, username: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanPhone = phoneNumber.trim()
        val cleanUsername = (if (username.isNotBlank()) username else "user_${cleanPhone.takeLast(4)}").trim().lowercase()

        try {
            // Check if user with this phone exists in Firestore
            val phoneQuery = FirebaseManager.firestore.collection(FirebaseManager.COL_USERS)
                .whereEqualTo("phoneNumber", cleanPhone).limit(1).get().await()

            if (!phoneQuery.isEmpty) {
                val doc = phoneQuery.documents[0]
                val existingUser = UserEntity(
                    id = doc.id,
                    username = doc.getString("username") ?: cleanUsername,
                    fullName = doc.getString("fullName") ?: "Crexa User",
                    bio = doc.getString("bio") ?: "Joined via verified phone number 📱",
                    avatarUrl = doc.getString("avatarUrl") ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                    email = doc.getString("email") ?: "$cleanUsername@crexa.app",
                    phoneNumber = cleanPhone,
                    followersCount = doc.getLong("followersCount")?.toInt() ?: 0,
                    followingCount = doc.getLong("followingCount")?.toInt() ?: 0,
                    postsCount = doc.getLong("postsCount")?.toInt() ?: 0,
                    isCurrentUser = true
                )
                userDao.clearCurrentUserFlag()
                userDao.insertUser(existingUser)
                _isLoggedIn.value = true
                return@withContext Result.success(existingUser)
            }

            // Create new phone user
            val uid = "phone_user_${UUID.randomUUID().toString().take(10)}"
            val newUser = UserEntity(
                id = uid,
                username = cleanUsername,
                fullName = cleanUsername.replaceFirstChar { it.uppercase() },
                bio = "Joined Crexa via Phone verification 📱✨",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                email = "$cleanUsername@crexa.app",
                phoneNumber = cleanPhone,
                isCurrentUser = true
            )

            FirebaseManager.saveUserProfile(newUser)
            userDao.clearCurrentUserFlag()
            userDao.insertUser(newUser)
            _isLoggedIn.value = true

            return@withContext Result.success(newUser)
        } catch (e: Throwable) {
            Log.e(TAG, "Phone login error: ${e.message}")
            val fallbackUser = UserEntity(
                id = "phone_${System.currentTimeMillis()}",
                username = cleanUsername,
                fullName = cleanUsername.replaceFirstChar { it.uppercase() },
                bio = "Joined Crexa via Phone verification 📱✨",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                email = "$cleanUsername@crexa.app",
                phoneNumber = cleanPhone,
                isCurrentUser = true
            )
            userDao.clearCurrentUserFlag()
            userDao.insertUser(fallbackUser)
            _isLoggedIn.value = true
            return@withContext Result.success(fallbackUser)
        }
    }

    suspend fun loginUser(username: String) = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase()
        val dummyPass = "crexa123456"
        val loginRes = loginWithCredentials(cleanUsername, dummyPass)
        if (loginRes.isFailure) {
            registerUser(cleanUsername, "$cleanUsername@crexa.app", dummyPass)
        }
    }

    suspend fun deleteCurrentAccount(): Boolean = withContext(Dispatchers.IO) {
        val current = userDao.getCurrentUser() ?: return@withContext false
        FirebaseManager.deleteUserAccount(current.id)
        userDao.clearCurrentUserFlag()
        userDao.deleteUser(current)
        _isLoggedIn.value = false
        true
    }

    suspend fun logoutUser() = withContext(Dispatchers.IO) {
        try {
            FirebaseManager.auth.signOut()
        } catch (e: Throwable) {
            Log.w(TAG, "Logout error: ${e.message}")
        }
        userDao.clearCurrentUserFlag()
        _isLoggedIn.value = false
    }

    suspend fun updateProfile(fullName: String, bio: String, website: String, avatarUrl: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser() ?: return@withContext
        var uploadedAvatar = avatarUrl

        if (avatarUrl.startsWith("content://") || avatarUrl.startsWith("file://")) {
            uploadedAvatar = FirebaseManager.uploadProfilePhoto(context, currentUser.id, Uri.parse(avatarUrl))
        }

        val updated = currentUser.copy(
            fullName = fullName,
            bio = bio,
            website = website,
            avatarUrl = if (uploadedAvatar.isNotBlank()) uploadedAvatar else currentUser.avatarUrl
        )
        userDao.updateUser(updated)
        FirebaseManager.saveUserProfile(updated)
    }

    suspend fun toggleFollowUser(userId: String) = withContext(Dispatchers.IO) {
        val current = userDao.getCurrentUser() ?: return@withContext
        val user = userDao.getUserById(userId) ?: return@withContext
        val newFollowState = !user.isFollowing

        userDao.updateFollowStatus(userId, newFollowState)
        FirebaseManager.toggleFollowUser(current.id, userId, newFollowState)
    }

    suspend fun toggleMuteUser(userId: String, isMuted: Boolean) = withContext(Dispatchers.IO) {
        userDao.updateMuteStatus(userId, isMuted)
    }

    suspend fun toggleBlockUser(userId: String, isBlocked: Boolean) = withContext(Dispatchers.IO) {
        userDao.updateBlockStatus(userId, isBlocked)
    }

    // -------------------------------------------------------------
    // Posts
    // -------------------------------------------------------------
    fun getAllPosts(): Flow<List<PostEntity>> = postDao.getAllPosts()
    fun getPostsByUser(userId: String): Flow<List<PostEntity>> = postDao.getPostsByUser(userId)
    fun getSavedPosts(): Flow<List<PostEntity>> = postDao.getSavedPosts()
    fun searchPosts(query: String): Flow<List<PostEntity>> = postDao.searchPosts(query)

    suspend fun createPost(mediaUrl: String, caption: String, location: String, hashtags: String, filterName: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser() ?: return@withContext
        val postId = "post_${System.currentTimeMillis()}"

        var finalMediaUrl = mediaUrl
        if (mediaUrl.startsWith("content://") || mediaUrl.startsWith("file://")) {
            finalMediaUrl = FirebaseManager.uploadPostMedia(context, currentUser.id, Uri.parse(mediaUrl))
        }

        val newPost = PostEntity(
            id = postId,
            userId = currentUser.id,
            mediaUrl = finalMediaUrl,
            caption = caption,
            location = location,
            hashtags = hashtags,
            filterName = filterName,
            timestamp = System.currentTimeMillis()
        )
        postDao.insertPost(newPost)
        userDao.updateUser(currentUser.copy(postsCount = currentUser.postsCount + 1))

        FirebaseManager.createPost(newPost)
    }

    suspend fun togglePostLike(postId: String, currentLiked: Boolean) = withContext(Dispatchers.IO) {
        val current = userDao.getCurrentUser() ?: return@withContext
        postDao.togglePostLike(postId, !currentLiked)
        FirebaseManager.togglePostLike(postId, current.id, !currentLiked)
    }

    suspend fun togglePostSave(postId: String, currentSaved: Boolean) = withContext(Dispatchers.IO) {
        postDao.togglePostSave(postId, !currentSaved)
    }

    suspend fun deletePost(postId: String) = withContext(Dispatchers.IO) {
        val current = userDao.getCurrentUser() ?: return@withContext
        postDao.deletePost(postId)
        FirebaseManager.deletePost(postId, current.id)
    }

    // -------------------------------------------------------------
    // Stories
    // -------------------------------------------------------------
    fun getAllStories(): Flow<List<StoryEntity>> = storyDao.getAllStories()

    suspend fun createStory(mediaUrl: String, caption: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser() ?: return@withContext
        val storyId = "story_${System.currentTimeMillis()}"

        var finalMediaUrl = mediaUrl
        if (mediaUrl.startsWith("content://") || mediaUrl.startsWith("file://")) {
            finalMediaUrl = FirebaseManager.uploadStoryMedia(context, currentUser.id, Uri.parse(mediaUrl))
        }

        val newStory = StoryEntity(
            id = storyId,
            userId = currentUser.id,
            mediaUrl = finalMediaUrl,
            caption = caption,
            timestamp = System.currentTimeMillis()
        )
        storyDao.insertStory(newStory)
        FirebaseManager.createStory(newStory)
    }

    suspend fun markStorySeen(storyId: String) = withContext(Dispatchers.IO) {
        val current = userDao.getCurrentUser() ?: return@withContext
        storyDao.markStorySeen(storyId)
        FirebaseManager.markStorySeen(storyId, current.id)
    }

    suspend fun getStorySeenUsers(storyId: String): List<UserEntity> = withContext(Dispatchers.IO) {
        FirebaseManager.getStorySeenUsers(storyId)
    }

    suspend fun getFollowersUsers(userId: String): List<UserEntity> = withContext(Dispatchers.IO) {
        FirebaseManager.getFollowersUsers(userId)
    }

    suspend fun getFollowingUsers(userId: String): List<UserEntity> = withContext(Dispatchers.IO) {
        FirebaseManager.getFollowingUsers(userId)
    }

    // -------------------------------------------------------------
    // Reels
    // -------------------------------------------------------------
    fun getAllReels(): Flow<List<ReelEntity>> = reelDao.getAllReels()
    fun getReelsByUser(userId: String): Flow<List<ReelEntity>> = reelDao.getReelsByUser(userId)
    fun getSavedReels(): Flow<List<ReelEntity>> = reelDao.getSavedReels()

    suspend fun createReel(videoUrl: String, caption: String, audioTitle: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser() ?: return@withContext
        val reelId = "reel_${System.currentTimeMillis()}"

        var finalVideoUrl = videoUrl
        if (videoUrl.startsWith("content://") || videoUrl.startsWith("file://")) {
            finalVideoUrl = FirebaseManager.uploadReelMedia(context, currentUser.id, Uri.parse(videoUrl))
        }

        val newReel = ReelEntity(
            id = reelId,
            userId = currentUser.id,
            videoUrl = finalVideoUrl,
            thumbnailUrl = finalVideoUrl,
            caption = caption,
            audioTitle = if (audioTitle.isBlank()) "Original Audio" else audioTitle,
            audioArtist = currentUser.fullName,
            timestamp = System.currentTimeMillis()
        )
        reelDao.insertReel(newReel)
        FirebaseManager.createReel(newReel)
    }

    suspend fun toggleReelLike(reelId: String, currentLiked: Boolean) = withContext(Dispatchers.IO) {
        val current = userDao.getCurrentUser() ?: return@withContext
        reelDao.toggleReelLike(reelId, !currentLiked)
        FirebaseManager.toggleReelLike(reelId, current.id, !currentLiked)
    }

    suspend fun toggleReelSave(reelId: String, currentSaved: Boolean) = withContext(Dispatchers.IO) {
        reelDao.toggleReelSave(reelId, !currentSaved)
    }

    // -------------------------------------------------------------
    // Comments
    // -------------------------------------------------------------
    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>> = commentDao.getCommentsForPost(postId)
    fun getCommentsForReel(reelId: String): Flow<List<CommentEntity>> = commentDao.getCommentsForReel(reelId)

    suspend fun addComment(postId: String?, reelId: String?, text: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser() ?: return@withContext
        val commentId = "comment_${System.currentTimeMillis()}"

        val newComment = CommentEntity(
            id = commentId,
            postId = postId,
            reelId = reelId,
            userId = currentUser.id,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        commentDao.insertComment(newComment)
        if (postId != null) postDao.incrementCommentsCount(postId)
        if (reelId != null) reelDao.incrementReelComments(reelId)

        FirebaseManager.addComment(newComment)
    }

    suspend fun toggleCommentLike(commentId: String, currentLiked: Boolean) = withContext(Dispatchers.IO) {
        commentDao.toggleCommentLike(commentId, !currentLiked)
    }

    // -------------------------------------------------------------
    // Notifications
    // -------------------------------------------------------------
    fun getAllNotifications(): Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    // -------------------------------------------------------------
    // Messages
    // -------------------------------------------------------------
    fun getMessagesBetweenUsers(userId1: String, userId2: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesBetweenUsers(userId1, userId2)

    fun getAllMessages(): Flow<List<MessageEntity>> = messageDao.getAllMessages()

    suspend fun sendMessage(receiverUserId: String, text: String, mediaUrl: String? = null) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser() ?: return@withContext
        val msgId = "msg_${System.currentTimeMillis()}"

        val newMessage = MessageEntity(
            id = msgId,
            senderUserId = currentUser.id,
            receiverUserId = receiverUserId,
            text = text,
            mediaUrl = mediaUrl,
            timestamp = System.currentTimeMillis()
        )
        messageDao.insertMessage(newMessage)
        FirebaseManager.sendMessage(newMessage)
    }

    fun setThemeMode(isDark: Boolean?) {
        _isDarkMode.value = isDark
    }
}
