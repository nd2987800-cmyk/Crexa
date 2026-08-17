package com.example.data.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.local.entities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

object FirebaseManager {
    private const val TAG = "FirebaseManager"

    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    // Collections
    const val COL_USERS = "users"
    const val COL_USERNAMES = "usernames"
    const val COL_POSTS = "posts"
    const val COL_REELS = "reels"
    const val COL_STORIES = "stories"
    const val COL_COMMENTS = "comments"
    const val COL_LIKES = "likes"
    const val COL_FOLLOWS = "follows"
    const val COL_NOTIFICATIONS = "notifications"
    const val COL_MESSAGES = "messages"

    // -------------------------------------------------------------
    // Firebase Storage Uploads
    // -------------------------------------------------------------
    suspend fun uploadMedia(
        context: Context,
        uri: Uri,
        storagePath: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val storageRef = storage.reference.child(storagePath)
            val uploadTask = storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Log.d(TAG, "Uploaded media successfully: $downloadUrl")
            return@withContext downloadUrl
        } catch (e: Throwable) {
            Log.w(TAG, "Firebase Storage upload failed: ${e.message}. Using original URI.")
            return@withContext uri.toString()
        }
    }

    suspend fun uploadProfilePhoto(context: Context, userId: String, uri: Uri): String {
        val path = "profiles/$userId/avatar_${System.currentTimeMillis()}.jpg"
        return uploadMedia(context, uri, path)
    }

    suspend fun uploadPostMedia(context: Context, userId: String, uri: Uri): String {
        val path = "posts/$userId/post_${UUID.randomUUID()}.jpg"
        return uploadMedia(context, uri, path)
    }

    suspend fun uploadReelMedia(context: Context, userId: String, uri: Uri): String {
        val path = "reels/$userId/reel_${UUID.randomUUID()}.mp4"
        return uploadMedia(context, uri, path)
    }

    suspend fun uploadStoryMedia(context: Context, userId: String, uri: Uri): String {
        val path = "stories/$userId/story_${UUID.randomUUID()}.jpg"
        return uploadMedia(context, uri, path)
    }

    // -------------------------------------------------------------
    // User Profiles & Auth in Firestore
    // -------------------------------------------------------------
    suspend fun saveUserProfile(user: UserEntity) = withContext(Dispatchers.IO) {
        try {
            val userMap = hashMapOf(
                "id" to user.id,
                "username" to user.username.lowercase().trim(),
                "email" to user.email.lowercase().trim(),
                "phoneNumber" to user.phoneNumber.trim(),
                "fullName" to user.fullName,
                "bio" to user.bio,
                "website" to user.website,
                "avatarUrl" to user.avatarUrl,
                "followersCount" to user.followersCount,
                "followingCount" to user.followingCount,
                "postsCount" to user.postsCount,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COL_USERS).document(user.id).set(userMap, SetOptions.merge()).await()

            // Index username for uniqueness checks
            val usernameDoc = hashMapOf("uid" to user.id, "createdAt" to System.currentTimeMillis())
            firestore.collection(COL_USERNAMES).document(user.username.lowercase().trim())
                .set(usernameDoc, SetOptions.merge()).await()

            Log.d(TAG, "Saved user profile to Firestore: ${user.id} (@${user.username})")
        } catch (e: Throwable) {
            Log.e(TAG, "Error saving user profile to Firestore: ${e.message}")
        }
    }

    suspend fun getUserProfile(userId: String): UserEntity? = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection(COL_USERS).document(userId).get().await()
            if (doc.exists()) {
                return@withContext UserEntity(
                    id = doc.id,
                    username = doc.getString("username") ?: "user",
                    fullName = doc.getString("fullName") ?: "Crexa User",
                    bio = doc.getString("bio") ?: "",
                    avatarUrl = doc.getString("avatarUrl") ?: "",
                    email = doc.getString("email") ?: "",
                    phoneNumber = doc.getString("phoneNumber") ?: "",
                    followersCount = doc.getLong("followersCount")?.toInt() ?: 0,
                    followingCount = doc.getLong("followingCount")?.toInt() ?: 0,
                    postsCount = doc.getLong("postsCount")?.toInt() ?: 0,
                    website = doc.getString("website") ?: "",
                    isCurrentUser = (auth.currentUser?.uid == doc.id)
                )
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error getting user profile: ${e.message}")
        }
        return@withContext null
    }

    suspend fun searchUsersByUsername(query: String): List<UserEntity> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim().lowercase().removePrefix("@")
        if (cleanQuery.isEmpty()) return@withContext emptyList()

        try {
            val currentAuthUid = auth.currentUser?.uid

            // Query Firestore users collection by username prefix
            val querySnapshot = firestore.collection(COL_USERS)
                .whereGreaterThanOrEqualTo("username", cleanQuery)
                .whereLessThanOrEqualTo("username", cleanQuery + "\uf8ff")
                .limit(30)
                .get()
                .await()

            val results = mutableListOf<UserEntity>()
            val foundIds = mutableSetOf<String>()

            for (doc in querySnapshot.documents) {
                val id = doc.id
                foundIds.add(id)
                results.add(
                    UserEntity(
                        id = id,
                        username = doc.getString("username") ?: "user",
                        fullName = doc.getString("fullName") ?: "Crexa User",
                        bio = doc.getString("bio") ?: "",
                        avatarUrl = doc.getString("avatarUrl") ?: "",
                        email = doc.getString("email") ?: "",
                        followersCount = doc.getLong("followersCount")?.toInt() ?: 0,
                        followingCount = doc.getLong("followingCount")?.toInt() ?: 0,
                        postsCount = doc.getLong("postsCount")?.toInt() ?: 0,
                        website = doc.getString("website") ?: "",
                        isCurrentUser = (id == currentAuthUid)
                    )
                )
            }

            // Also search fallback across recent users for substring/full name matches
            val fallbackSnapshot = firestore.collection(COL_USERS).limit(50).get().await()
            for (doc in fallbackSnapshot.documents) {
                val id = doc.id
                if (id !in foundIds) {
                    val username = doc.getString("username") ?: ""
                    val fullName = doc.getString("fullName") ?: ""
                    if (username.contains(cleanQuery, ignoreCase = true) || fullName.contains(cleanQuery, ignoreCase = true)) {
                        foundIds.add(id)
                        results.add(
                            UserEntity(
                                id = id,
                                username = if (username.isEmpty()) "user" else username,
                                fullName = if (fullName.isEmpty()) "Crexa User" else fullName,
                                bio = doc.getString("bio") ?: "",
                                avatarUrl = doc.getString("avatarUrl") ?: "",
                                email = doc.getString("email") ?: "",
                                followersCount = doc.getLong("followersCount")?.toInt() ?: 0,
                                followingCount = doc.getLong("followingCount")?.toInt() ?: 0,
                                postsCount = doc.getLong("postsCount")?.toInt() ?: 0,
                                website = doc.getString("website") ?: "",
                                isCurrentUser = (id == currentAuthUid)
                            )
                        )
                    }
                }
            }

            // Check follow state in Firestore for current auth user
            if (currentAuthUid != null && results.isNotEmpty()) {
                for (i in results.indices) {
                    val user = results[i]
                    if (!user.isCurrentUser) {
                        val followDocId = "${currentAuthUid}_${user.id}"
                        val followDoc = firestore.collection(COL_FOLLOWS).document(followDocId).get().await()
                        results[i] = user.copy(isFollowing = followDoc.exists())
                    }
                }
            }

            return@withContext results
        } catch (e: Throwable) {
            Log.e(TAG, "Error searching users by username in Firestore: ${e.message}")
            return@withContext emptyList()
        }
    }

    suspend fun deleteUserAccount(userId: String) = withContext(Dispatchers.IO) {
        try {
            // Delete user doc
            firestore.collection(COL_USERS).document(userId).delete().await()

            // Delete user's posts
            val postsQuery = firestore.collection(COL_POSTS).whereEqualTo("userId", userId).get().await()
            for (p in postsQuery.documents) {
                p.reference.delete()
            }

            // Delete user's reels
            val reelsQuery = firestore.collection(COL_REELS).whereEqualTo("userId", userId).get().await()
            for (r in reelsQuery.documents) {
                r.reference.delete()
            }

            // Delete user's stories
            val storiesQuery = firestore.collection(COL_STORIES).whereEqualTo("userId", userId).get().await()
            for (s in storiesQuery.documents) {
                s.reference.delete()
            }

            // Delete Firebase Auth user
            auth.currentUser?.delete()?.await()
            Log.d(TAG, "Deleted user account $userId from Firebase")
        } catch (e: Throwable) {
            Log.e(TAG, "Error deleting user account from Firebase: ${e.message}")
        }
    }

    // -------------------------------------------------------------
    // Posts
    // -------------------------------------------------------------
    suspend fun createPost(post: PostEntity) = withContext(Dispatchers.IO) {
        try {
            val postMap = hashMapOf(
                "id" to post.id,
                "userId" to post.userId,
                "mediaUrl" to post.mediaUrl,
                "caption" to post.caption,
                "location" to post.location,
                "hashtags" to post.hashtags,
                "filterName" to post.filterName,
                "likesCount" to post.likesCount,
                "commentsCount" to post.commentsCount,
                "timestamp" to post.timestamp
            )
            firestore.collection(COL_POSTS).document(post.id).set(postMap).await()

            // Increment user's postsCount
            firestore.collection(COL_USERS).document(post.userId)
                .update("postsCount", FieldValue.increment(1))
            Log.d(TAG, "Created post ${post.id} in Firestore")
        } catch (e: Throwable) {
            Log.e(TAG, "Error creating post in Firestore: ${e.message}")
        }
    }

    suspend fun deletePost(postId: String, userId: String) = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COL_POSTS).document(postId).delete().await()
            firestore.collection(COL_USERS).document(userId)
                .update("postsCount", FieldValue.increment(-1))
            Log.d(TAG, "Deleted post $postId from Firestore")
        } catch (e: Throwable) {
            Log.e(TAG, "Error deleting post $postId: ${e.message}")
        }
    }

    suspend fun togglePostLike(postId: String, userId: String, isLiked: Boolean) = withContext(Dispatchers.IO) {
        try {
            val likeDocId = "post_${postId}_${userId}"
            val likeRef = firestore.collection(COL_LIKES).document(likeDocId)
            val postRef = firestore.collection(COL_POSTS).document(postId)

            if (isLiked) {
                val likeData = hashMapOf(
                    "id" to likeDocId,
                    "targetType" to "POST",
                    "targetId" to postId,
                    "userId" to userId,
                    "timestamp" to System.currentTimeMillis()
                )
                likeRef.set(likeData).await()
                postRef.update("likesCount", FieldValue.increment(1))
            } else {
                likeRef.delete().await()
                postRef.update("likesCount", FieldValue.increment(-1))
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error toggling post like: ${e.message}")
        }
    }

    // -------------------------------------------------------------
    // Reels
    // -------------------------------------------------------------
    suspend fun createReel(reel: ReelEntity) = withContext(Dispatchers.IO) {
        try {
            val reelMap = hashMapOf(
                "id" to reel.id,
                "userId" to reel.userId,
                "videoUrl" to reel.videoUrl,
                "thumbnailUrl" to reel.thumbnailUrl,
                "caption" to reel.caption,
                "audioTitle" to reel.audioTitle,
                "audioArtist" to reel.audioArtist,
                "likesCount" to reel.likesCount,
                "commentsCount" to reel.commentsCount,
                "timestamp" to reel.timestamp
            )
            firestore.collection(COL_REELS).document(reel.id).set(reelMap).await()
            Log.d(TAG, "Created reel ${reel.id} in Firestore")
        } catch (e: Throwable) {
            Log.e(TAG, "Error creating reel in Firestore: ${e.message}")
        }
    }

    suspend fun toggleReelLike(reelId: String, userId: String, isLiked: Boolean) = withContext(Dispatchers.IO) {
        try {
            val likeDocId = "reel_${reelId}_${userId}"
            val likeRef = firestore.collection(COL_LIKES).document(likeDocId)
            val reelRef = firestore.collection(COL_REELS).document(reelId)

            if (isLiked) {
                val likeData = hashMapOf(
                    "id" to likeDocId,
                    "targetType" to "REEL",
                    "targetId" to reelId,
                    "userId" to userId,
                    "timestamp" to System.currentTimeMillis()
                )
                likeRef.set(likeData).await()
                reelRef.update("likesCount", FieldValue.increment(1))
            } else {
                likeRef.delete().await()
                reelRef.update("likesCount", FieldValue.increment(-1))
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error toggling reel like: ${e.message}")
        }
    }

    // -------------------------------------------------------------
    // Stories
    // -------------------------------------------------------------
    suspend fun createStory(story: StoryEntity) = withContext(Dispatchers.IO) {
        try {
            val storyMap = hashMapOf(
                "id" to story.id,
                "userId" to story.userId,
                "mediaUrl" to story.mediaUrl,
                "caption" to story.caption,
                "timestamp" to story.timestamp,
                "seenUserIds" to listOf<String>()
            )
            firestore.collection(COL_STORIES).document(story.id).set(storyMap).await()
            Log.d(TAG, "Created story ${story.id} in Firestore")
        } catch (e: Throwable) {
            Log.e(TAG, "Error creating story in Firestore: ${e.message}")
        }
    }

    suspend fun markStorySeen(storyId: String, userId: String) = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COL_STORIES).document(storyId)
                .update("seenUserIds", FieldValue.arrayUnion(userId))
        } catch (e: Throwable) {
            Log.e(TAG, "Error marking story seen: ${e.message}")
        }
    }

    suspend fun getStorySeenUsers(storyId: String): List<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val storyDoc = firestore.collection(COL_STORIES).document(storyId).get().await()
            if (!storyDoc.exists()) return@withContext emptyList()
            
            @Suppress("UNCHECKED_CAST")
            val seenUserIds = (storyDoc.get("seenUserIds") as? List<String>) ?: emptyList()
            if (seenUserIds.isEmpty()) return@withContext emptyList()

            val users = mutableListOf<UserEntity>()
            val currentAuthUid = auth.currentUser?.uid

            // Fetch user documents for each seen ID
            for (uid in seenUserIds) {
                val userDoc = firestore.collection(COL_USERS).document(uid).get().await()
                if (userDoc.exists()) {
                    users.add(
                        UserEntity(
                            id = userDoc.id,
                            username = userDoc.getString("username") ?: "user",
                            fullName = userDoc.getString("fullName") ?: "Crexa User",
                            bio = userDoc.getString("bio") ?: "",
                            avatarUrl = userDoc.getString("avatarUrl") ?: "",
                            email = userDoc.getString("email") ?: "",
                            followersCount = userDoc.getLong("followersCount")?.toInt() ?: 0,
                            followingCount = userDoc.getLong("followingCount")?.toInt() ?: 0,
                            postsCount = userDoc.getLong("postsCount")?.toInt() ?: 0,
                            website = userDoc.getString("website") ?: "",
                            isCurrentUser = (userDoc.id == currentAuthUid)
                        )
                    )
                }
            }
            return@withContext users
        } catch (e: Throwable) {
            Log.e(TAG, "Error fetching seen users for story $storyId: ${e.message}")
            return@withContext emptyList()
        }
    }

    suspend fun getFollowersUsers(userId: String): List<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val followsQuery = firestore.collection(COL_FOLLOWS)
                .whereEqualTo("targetUserId", userId)
                .get()
                .await()

            val followerIds = followsQuery.documents.mapNotNull { it.getString("followerId") }
            if (followerIds.isEmpty()) return@withContext emptyList()

            val currentAuthUid = auth.currentUser?.uid
            val users = mutableListOf<UserEntity>()

            for (fid in followerIds) {
                val userDoc = firestore.collection(COL_USERS).document(fid).get().await()
                if (userDoc.exists()) {
                    val isFollowingBack = if (currentAuthUid != null) {
                        val checkDoc = firestore.collection(COL_FOLLOWS).document("${currentAuthUid}_${fid}").get().await()
                        checkDoc.exists()
                    } else false

                    users.add(
                        UserEntity(
                            id = userDoc.id,
                            username = userDoc.getString("username") ?: "user",
                            fullName = userDoc.getString("fullName") ?: "Crexa User",
                            bio = userDoc.getString("bio") ?: "",
                            avatarUrl = userDoc.getString("avatarUrl") ?: "",
                            email = userDoc.getString("email") ?: "",
                            followersCount = userDoc.getLong("followersCount")?.toInt() ?: 0,
                            followingCount = userDoc.getLong("followingCount")?.toInt() ?: 0,
                            postsCount = userDoc.getLong("postsCount")?.toInt() ?: 0,
                            website = userDoc.getString("website") ?: "",
                            isFollowing = isFollowingBack,
                            isCurrentUser = (userDoc.id == currentAuthUid)
                        )
                    )
                }
            }
            return@withContext users
        } catch (e: Throwable) {
            Log.e(TAG, "Error getting followers for $userId: ${e.message}")
            return@withContext emptyList()
        }
    }

    suspend fun getFollowingUsers(userId: String): List<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val followsQuery = firestore.collection(COL_FOLLOWS)
                .whereEqualTo("followerId", userId)
                .get()
                .await()

            val followingIds = followsQuery.documents.mapNotNull { it.getString("targetUserId") }
            if (followingIds.isEmpty()) return@withContext emptyList()

            val currentAuthUid = auth.currentUser?.uid
            val users = mutableListOf<UserEntity>()

            for (tid in followingIds) {
                val userDoc = firestore.collection(COL_USERS).document(tid).get().await()
                if (userDoc.exists()) {
                    val isFollowingBack = if (currentAuthUid != null) {
                        val checkDoc = firestore.collection(COL_FOLLOWS).document("${currentAuthUid}_${tid}").get().await()
                        checkDoc.exists()
                    } else false

                    users.add(
                        UserEntity(
                            id = userDoc.id,
                            username = userDoc.getString("username") ?: "user",
                            fullName = userDoc.getString("fullName") ?: "Crexa User",
                            bio = userDoc.getString("bio") ?: "",
                            avatarUrl = userDoc.getString("avatarUrl") ?: "",
                            email = userDoc.getString("email") ?: "",
                            followersCount = userDoc.getLong("followersCount")?.toInt() ?: 0,
                            followingCount = userDoc.getLong("followingCount")?.toInt() ?: 0,
                            postsCount = userDoc.getLong("postsCount")?.toInt() ?: 0,
                            website = userDoc.getString("website") ?: "",
                            isFollowing = isFollowingBack,
                            isCurrentUser = (userDoc.id == currentAuthUid)
                        )
                    )
                }
            }
            return@withContext users
        } catch (e: Throwable) {
            Log.e(TAG, "Error getting following for $userId: ${e.message}")
            return@withContext emptyList()
        }
    }

    // -------------------------------------------------------------
    // Comments
    // -------------------------------------------------------------
    suspend fun addComment(comment: CommentEntity) = withContext(Dispatchers.IO) {
        try {
            val commentMap = hashMapOf(
                "id" to comment.id,
                "postId" to (comment.postId ?: ""),
                "reelId" to (comment.reelId ?: ""),
                "userId" to comment.userId,
                "text" to comment.text,
                "timestamp" to comment.timestamp,
                "likesCount" to comment.likesCount
            )
            firestore.collection(COL_COMMENTS).document(comment.id).set(commentMap).await()

            if (!comment.postId.isNullOrEmpty()) {
                firestore.collection(COL_POSTS).document(comment.postId)
                    .update("commentsCount", FieldValue.increment(1))
            }
            if (!comment.reelId.isNullOrEmpty()) {
                firestore.collection(COL_REELS).document(comment.reelId)
                    .update("commentsCount", FieldValue.increment(1))
            }
            Log.d(TAG, "Added comment ${comment.id} to Firestore")
        } catch (e: Throwable) {
            Log.e(TAG, "Error adding comment: ${e.message}")
        }
    }

    // -------------------------------------------------------------
    // Follows & Followers
    // -------------------------------------------------------------
    suspend fun toggleFollowUser(currentUserId: String, targetUserId: String, isFollowing: Boolean) = withContext(Dispatchers.IO) {
        try {
            val followDocId = "${currentUserId}_${targetUserId}"
            val followRef = firestore.collection(COL_FOLLOWS).document(followDocId)

            if (isFollowing) {
                val followData = hashMapOf(
                    "id" to followDocId,
                    "followerId" to currentUserId,
                    "targetUserId" to targetUserId,
                    "timestamp" to System.currentTimeMillis()
                )
                followRef.set(followData).await()

                // Increment following for current, followers for target
                firestore.collection(COL_USERS).document(currentUserId)
                    .update("followingCount", FieldValue.increment(1))
                firestore.collection(COL_USERS).document(targetUserId)
                    .update("followersCount", FieldValue.increment(1))

                // Create follow notification
                sendNotification(
                    NotificationEntity(
                        id = "notif_${System.currentTimeMillis()}",
                        recipientUserId = targetUserId,
                        actorUserId = currentUserId,
                        type = "FOLLOW",
                        message = "started following you.",
                        timestamp = System.currentTimeMillis()
                    )
                )
            } else {
                followRef.delete().await()
                firestore.collection(COL_USERS).document(currentUserId)
                    .update("followingCount", FieldValue.increment(-1))
                firestore.collection(COL_USERS).document(targetUserId)
                    .update("followersCount", FieldValue.increment(-1))
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error toggling follow status: ${e.message}")
        }
    }

    // -------------------------------------------------------------
    // Direct Messages & Chats
    // -------------------------------------------------------------
    suspend fun sendMessage(message: MessageEntity) = withContext(Dispatchers.IO) {
        try {
            val msgMap = hashMapOf(
                "id" to message.id,
                "senderUserId" to message.senderUserId,
                "receiverUserId" to message.receiverUserId,
                "text" to message.text,
                "mediaUrl" to (message.mediaUrl ?: ""),
                "timestamp" to message.timestamp,
                "isRead" to message.isRead
            )
            firestore.collection(COL_MESSAGES).document(message.id).set(msgMap).await()
            Log.d(TAG, "Sent message ${message.id} to Firestore")
        } catch (e: Throwable) {
            Log.e(TAG, "Error sending message: ${e.message}")
        }
    }

    // -------------------------------------------------------------
    // Notifications
    // -------------------------------------------------------------
    suspend fun sendNotification(notification: NotificationEntity) = withContext(Dispatchers.IO) {
        try {
            val notifMap = hashMapOf(
                "id" to notification.id,
                "recipientUserId" to notification.recipientUserId,
                "actorUserId" to notification.actorUserId,
                "type" to notification.type,
                "message" to notification.message,
                "postOrReelId" to (notification.postOrReelId ?: ""),
                "timestamp" to notification.timestamp,
                "isRead" to notification.isRead
            )
            firestore.collection(COL_NOTIFICATIONS).document(notification.id).set(notifMap).await()
        } catch (e: Throwable) {
            Log.e(TAG, "Error sending notification: ${e.message}")
        }
    }
}
