package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val fullName: String,
    val bio: String,
    val avatarUrl: String,
    val passwordHash: String = "",
    val email: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val isVerified: Boolean = false,
    val isFollowing: Boolean = false,
    val isCurrentUser: Boolean = false,
    val website: String = "",
    val isPrivate: Boolean = false,
    val isMuted: Boolean = false,
    val isBlocked: Boolean = false
)
