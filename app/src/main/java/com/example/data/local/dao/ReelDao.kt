package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.ReelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReelDao {
    @Query("SELECT * FROM reels ORDER BY timestamp DESC")
    fun getAllReels(): Flow<List<ReelEntity>>

    @Query("SELECT * FROM reels WHERE userId = :userId ORDER BY timestamp DESC")
    fun getReelsByUser(userId: String): Flow<List<ReelEntity>>

    @Query("SELECT * FROM reels WHERE isSaved = 1 ORDER BY timestamp DESC")
    fun getSavedReels(): Flow<List<ReelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReel(reel: ReelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReels(reels: List<ReelEntity>)

    @Query("UPDATE reels SET isLiked = :isLiked, likesCount = likesCount + (CASE WHEN :isLiked THEN 1 ELSE -1 END) WHERE id = :reelId")
    suspend fun toggleReelLike(reelId: String, isLiked: Boolean)

    @Query("UPDATE reels SET isSaved = :isSaved WHERE id = :reelId")
    suspend fun toggleReelSave(reelId: String, isSaved: Boolean)

    @Query("UPDATE reels SET commentsCount = commentsCount + 1 WHERE id = :reelId")
    suspend fun incrementReelComments(reelId: String)
}
