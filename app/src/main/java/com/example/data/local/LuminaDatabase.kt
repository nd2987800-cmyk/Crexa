package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        StoryEntity::class,
        ReelEntity::class,
        CommentEntity::class,
        NotificationEntity::class,
        MessageEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class CrexaDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun storyDao(): StoryDao
    abstract fun reelDao(): ReelDao
    abstract fun commentDao(): CommentDao
    abstract fun notificationDao(): NotificationDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: CrexaDatabase? = null

        fun getDatabase(context: Context): CrexaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CrexaDatabase::class.java,
                    "crexa_social_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
