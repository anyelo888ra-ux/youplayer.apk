package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteVideoEntity::class,
        WatchHistoryEntity::class,
        OfflineCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class YouPlayerDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun offlineCacheDao(): OfflineCacheDao

    companion object {
        @Volatile
        private var INSTANCE: YouPlayerDatabase? = null

        fun getDatabase(context: Context): YouPlayerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    YouPlayerDatabase::class.java,
                    "youplayer_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
