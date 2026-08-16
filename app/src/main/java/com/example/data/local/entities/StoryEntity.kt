package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val mediaUrl: String,
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSeen: Boolean = false
)
