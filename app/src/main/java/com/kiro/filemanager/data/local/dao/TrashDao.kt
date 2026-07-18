package com.kiro.filemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kiro.filemanager.data.local.entity.TrashEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrashDao {
    @Query("SELECT * FROM recycle_bin ORDER BY deletedAt DESC")
    fun observeAll(): Flow<List<TrashEntity>>

    @Insert
    suspend fun insert(entity: TrashEntity): Long

    @Query("SELECT * FROM recycle_bin WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<TrashEntity>

    @Query("SELECT * FROM recycle_bin")
    suspend fun getAll(): List<TrashEntity>

    @Query("DELETE FROM recycle_bin WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM recycle_bin")
    suspend fun deleteAll()

    @Query("SELECT * FROM recycle_bin WHERE deletedAt < :threshold")
    suspend fun getExpired(threshold: Long): List<TrashEntity>
}
