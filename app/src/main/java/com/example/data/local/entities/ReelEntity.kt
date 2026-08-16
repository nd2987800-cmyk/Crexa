package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reels")
data class ReelEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val caption: String = "",
    val audioTitle: String = "Original Audio",
    val audioArtist: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
