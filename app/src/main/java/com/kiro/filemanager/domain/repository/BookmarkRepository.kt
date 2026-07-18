package com.kiro.filemanager.domain.repository

import com.kiro.filemanager.domain.model.FileItem
import kotlinx.coroutines.flow.Flow

/** Favorites and recent-files persistence. */
interface BookmarkRepository {
    fun observeFavorites(): Flow<List<FileItem>>
    fun observeFavoritePaths(): Flow<Set<String>>
    suspend fun addFavorite(item: FileItem)
    suspend fun removeFavorite(path: String)
    suspend fun isFavorite(path: String): Boolean

    fun observeRecent(limit: Int = 50): Flow<List<FileItem>>
    suspend fun recordAccess(item: FileItem)
    suspend fun clearRecent()
}
