package com.kiro.filemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kiro.filemanager.data.local.entity.RecentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentDao {
    @Query("SELECT * FROM recent_files ORDER BY accessedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<RecentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecentEntity)

    @Query("DELETE FROM recent_files")
    suspend fun clear()

    /** Trims history to the newest [keep] rows. */
    @Query(
        "DELETE FROM recent_files WHERE path NOT IN " +
            "(SELECT path FROM recent_files ORDER BY accessedAt DESC LIMIT :keep)"
    )
    suspend fun trim(keep: Int)
}
