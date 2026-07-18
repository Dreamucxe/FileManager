package com.kiro.filemanager.data.repository

import android.content.Context
import com.kiro.filemanager.core.util.IoDispatcher
import com.kiro.filemanager.data.local.dao.TrashDao
import com.kiro.filemanager.data.local.entity.TrashEntity
import com.kiro.filemanager.domain.model.OperationResult
import com.kiro.filemanager.domain.model.TrashItem
import com.kiro.filemanager.domain.repository.RecycleBinRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recycle bin backed by a physical trash directory in the app's files dir plus
 * a Room index. Moving to trash is a rename when possible, else a copy+delete.
 */
@Singleton
class RecycleBinRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val trashDao: TrashDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : RecycleBinRepository {

    private val trashDir: File = File(context.filesDir, "recycle_bin").apply { mkdirs() }

    override fun observeTrash(): Flow<List<TrashItem>> =
        trashDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun moveToTrash(paths: List<String>): Result<List<Long>> =
        withContext(io) {
            runCatching {
                val ids = mutableListOf<Long>()
                for (path in paths) {
                    val src = File(path)
                    if (!src.exists()) continue
                    val stamp = System.currentTimeMillis()
                    val dest = File(trashDir, "${stamp}_${src.name}")
                    val size = if (src.isDirectory) folderSize(src) else src.length()
                    if (!src.renameTo(dest)) {
                        src.copyRecursively(dest, overwrite = true)
                        if (src.isDirectory) src.deleteRecursively() else src.delete()
                    }
                    val id = trashDao.insert(
                        TrashEntity(
                            originalPath = path,
                            trashPath = dest.absolutePath,
                            name = src.name,
                            size = size,
                            isDirectory = src.isDirectory,
                            deletedAt = stamp,
                        )
                    )
                    ids += id
                }
                ids
            }
        }

    override suspend fun restore(ids: List<Long>): OperationResult = withContext(io) {
        runCatching {
            val entities = trashDao.getByIds(ids)
            val restored = mutableListOf<String>()
            for (e in entities) {
                val src = File(e.trashPath)
                val dest = File(e.originalPath)
                dest.parentFile?.mkdirs()
                if (!src.renameTo(dest)) {
                    src.copyRecursively(dest, overwrite = true)
                    if (src.isDirectory) src.deleteRecursively() else src.delete()
                }
                restored += e.originalPath
            }
            trashDao.deleteByIds(ids)
            OperationResult.Success(restored)
        }.getOrElse { OperationResult.Failure(it.message ?: "Restore failed", it) }
    }

    override suspend fun deletePermanently(ids: List<Long>): OperationResult = withContext(io) {
        runCatching {
            val entities = trashDao.getByIds(ids)
            entities.forEach { e ->
                val f = File(e.trashPath)
                if (f.isDirectory) f.deleteRecursively() else f.delete()
            }
            trashDao.deleteByIds(ids)
            OperationResult.Success(entities.map { it.originalPath })
        }.getOrElse { OperationResult.Failure(it.message ?: "Delete failed", it) }
    }

    override suspend fun emptyBin(): OperationResult = withContext(io) {
        runCatching {
            trashDao.getAll().forEach { e ->
                val f = File(e.trashPath)
                if (f.isDirectory) f.deleteRecursively() else f.delete()
            }
            trashDao.deleteAll()
            OperationResult.Success(emptyList())
        }.getOrElse { OperationResult.Failure(it.message ?: "Empty failed", it) }
    }

    override suspend fun purgeExpired(maxAgeMillis: Long) = withContext(io) {
        val threshold = System.currentTimeMillis() - maxAgeMillis
        val expired = trashDao.getExpired(threshold)
        expired.forEach { e ->
            val f = File(e.trashPath)
            if (f.isDirectory) f.deleteRecursively() else f.delete()
        }
        trashDao.deleteByIds(expired.map { it.id })
    }

    private fun folderSize(dir: File): Long {
        var total = 0L
        dir.walkTopDown().forEach { if (it.isFile) total += it.length() }
        return total
    }

    private fun TrashEntity.toDomain() = TrashItem(
        id = id,
        originalPath = originalPath,
        trashPath = trashPath,
        name = name,
        size = size,
        isDirectory = isDirectory,
        deletedAt = deletedAt,
    )
}
