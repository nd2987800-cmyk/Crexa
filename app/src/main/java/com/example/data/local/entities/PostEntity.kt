package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val mediaUrl: String,
    val mediaType: String = "IMAGE", // IMAGE, VIDEO
    val caption: String = "",
    val hashtags: String = "",
    val location: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val filterName: String = "Normal"
)
