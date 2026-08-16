package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.CommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments WHERE reelId = :reelId ORDER BY timestamp ASC")
    fun getCommentsForReel(reelId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    @Query("UPDATE comments SET isLiked = :isLiked, likesCount = likesCount + (CASE WHEN :isLiked THEN 1 ELSE -1 END) WHERE id = :commentId")
    suspend fun toggleCommentLike(commentId: String, isLiked: Boolean)
}
