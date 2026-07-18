package com.kiro.filemanager.data.repository

import com.kiro.filemanager.core.util.IoDispatcher
import com.kiro.filemanager.core.util.MimeTypes
import com.kiro.filemanager.data.datasource.LocalFileDataSource
import com.kiro.filemanager.data.local.dao.FavoriteDao
import com.kiro.filemanager.data.local.dao.RecentDao
import com.kiro.filemanager.data.local.entity.FavoriteEntity
import com.kiro.filemanager.data.local.entity.RecentEntity
import com.kiro.filemanager.domain.model.FileItem
import com.kiro.filemanager.domain.repository.BookmarkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val recentDao: RecentDao,
    private val local: LocalFileDataSource,
    @IoDispatcher private val io: CoroutineDispatcher,
) : BookmarkRepository {

    override fun observeFavorites(): Flow<List<FileItem>> =
        favoriteDao.observeAll().map { list ->
            // Resolve live metadata; skip entries whose backing file is gone.
            list.mapNotNull { local.getItem(it.path) ?: it.toStaleItem() }
                .map { it.copy(isFavorite = true) }
        }

    override fun observeFavoritePaths(): Flow<Set<String>> =
        favoriteDao.observePaths().map { it.toSet() }

    override suspend fun addFavorite(item: FileItem) = withContext(io) {
        favoriteDao.insert(
            FavoriteEntity(
                path = item.path,
                name = item.name,
                isDirectory = item.isDirectory,
                addedAt = System.currentTimeMillis(),
            )
        )
    }

    override suspend fun removeFavorite(path: String) = withContext(io) {
        favoriteDao.delete(path)
    }

    override suspend fun isFavorite(path: String): Boolean = withContext(io) {
        favoriteDao.exists(path)
    }

    override fun observeRecent(limit: Int): Flow<List<FileItem>> =
        recentDao.observeRecent(limit).map { list ->
            list.mapNotNull { local.getItem(it.path) }
        }

    override suspend fun recordAccess(item: FileItem) = withContext(io) {
        if (item.isDirectory) return@withContext
        recentDao.upsert(
            RecentEntity(
                path = item.path,
                name = item.name,
                isDirectory = false,
                accessedAt = System.currentTimeMillis(),
            )
        )
        recentDao.trim(MAX_RECENT)
    }

    override suspend fun clearRecent() = withContext(io) { recentDao.clear() }

    private fun FavoriteEntity.toStaleItem(): FileItem? {
        // Keep favorites visible even if temporarily unmounted.
        val ext = if (isDirectory) "" else name.substringAfterLast('.', "")
        return FileItem(
            path = path,
            name = name,
            isDirectory = isDirectory,
            size = 0,
            lastModified = 0,
            extension = ext,
            mimeType = if (isDirectory) "resource/folder" else MimeTypes.fromExtension(ext),
            isHidden = name.startsWith('.'),
            canRead = false,
            canWrite = false,
        )
    }

    private companion object {
        const val MAX_RECENT = 100
    }
}
