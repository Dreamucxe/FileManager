package com.kiro.filemanager.domain.repository

import com.kiro.filemanager.domain.model.OperationResult
import com.kiro.filemanager.domain.model.TrashItem
import kotlinx.coroutines.flow.Flow

/** App-managed recycle bin so deletes are recoverable. */
interface RecycleBinRepository {
    fun observeTrash(): Flow<List<TrashItem>>

    /** Moves the given paths into the trash directory. Returns new trash row ids. */
    suspend fun moveToTrash(paths: List<String>): Result<List<Long>>

    suspend fun restore(ids: List<Long>): OperationResult

    suspend fun deletePermanently(ids: List<Long>): OperationResult

    suspend fun emptyBin(): OperationResult

    /** Removes trash older than [maxAgeMillis]. */
    suspend fun purgeExpired(maxAgeMillis: Long)
}
