package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteVideoEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    fun isFavorite(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    suspend fun isFavoriteSync(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteVideoEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM favorites")
    suspend fun clearAll()
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT 100")
    fun getHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}

@Dao
interface OfflineCacheDao {
    @Query("SELECT * FROM offline_cache ORDER BY cachedAt DESC")
    fun getAllCached(): Flow<List<OfflineCacheEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM offline_cache WHERE id = :id)")
    fun isCached(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cacheItem: OfflineCacheEntity)

    @Query("DELETE FROM offline_cache WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM offline_cache")
    suspend fun clearAll()
}
