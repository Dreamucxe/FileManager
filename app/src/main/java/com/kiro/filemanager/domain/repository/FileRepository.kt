package com.kiro.filemanager.domain.repository

import com.kiro.filemanager.domain.model.FileItem
import com.kiro.filemanager.domain.model.OperationResult
import com.kiro.filemanager.domain.model.SearchQuery
import com.kiro.filemanager.domain.model.StorageVolumeInfo
import kotlinx.coroutines.flow.Flow

/**
 * Core file-system operations. Implementations pick the right access mechanism
 * (java.io for owned paths, SAF for restricted trees, Shizuku when available).
 */
interface FileRepository {

    /** Lists a directory, reacting to file-system changes where possible. */
    fun observeDirectory(path: String, showHidden: Boolean): Flow<List<FileItem>>

    suspend fun listDirectory(path: String, showHidden: Boolean): List<FileItem>

    suspend fun getFile(path: String): FileItem?

    suspend fun getMountedVolumes(): List<StorageVolumeInfo>

    /** Recursively computes the size of a directory tree. */
    suspend fun computeSize(path: String): Long

    suspend fun createFolder(parentPath: String, name: String): OperationResult

    suspend fun createFile(parentPath: String, name: String): OperationResult

    suspend fun rename(path: String, newName: String): OperationResult

    /** Copies items into [destinationDir], emitting progress. */
    fun copy(sources: List<String>, destinationDir: String): Flow<OperationResult>

    /** Moves items into [destinationDir], emitting progress. */
    fun move(sources: List<String>, destinationDir: String): Flow<OperationResult>

    suspend fun search(query: SearchQuery): Flow<List<FileItem>>

    fun readText(path: String): Flow<Result<String>>

    suspend fun writeText(path: String, content: String): OperationResult

    /** Reads a window of raw bytes for the hex viewer. */
    suspend fun readBytes(path: String, offset: Long, length: Int): ByteArray
}
