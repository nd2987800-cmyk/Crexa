package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE (senderUserId = :userId1 AND receiverUserId = :userId2) OR (senderUserId = :userId2 AND receiverUserId = :userId1) ORDER BY timestamp ASC")
    fun getMessagesBetweenUsers(userId1: String, userId2: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
}
