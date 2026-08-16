package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val recipientUserId: String,
    val actorUserId: String,
    val type: String, // LIKE, COMMENT, FOLLOW, MENTION
    val postOrReelId: String? = null,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
