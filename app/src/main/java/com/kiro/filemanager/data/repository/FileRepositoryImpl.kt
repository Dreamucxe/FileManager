package com.kiro.filemanager.data.repository

import com.kiro.filemanager.core.util.IoDispatcher
import com.kiro.filemanager.data.datasource.LocalFileDataSource
import com.kiro.filemanager.data.datasource.StorageVolumeDataSource
import com.kiro.filemanager.domain.model.FileItem
import com.kiro.filemanager.domain.model.OperationResult
import com.kiro.filemanager.domain.model.SearchQuery
import com.kiro.filemanager.domain.model.StorageVolumeInfo
import com.kiro.filemanager.domain.repository.FileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Primary [FileRepository] implementation over [LocalFileDataSource]. Directory
 * observation uses a lightweight polling watcher (FileObserver is unreliable
 * across the emulated storage layer on many devices).
 */
@Singleton
class FileRepositoryImpl @Inject constructor(
    private val local: LocalFileDataSource,
    private val storage: StorageVolumeDataSource,
    @IoDispatcher private val io: CoroutineDispatcher,
) : FileRepository {

    override fun observeDirectory(path: String, showHidden: Boolean): Flow<List<FileItem>> =
        callbackFlow {
            var lastSignature = 0L
            fun snapshot(): List<FileItem> = local.list(path, showHidden)
            fun signature(items: List<FileItem>): Long =
                items.fold(0L) { acc, it -> acc * 31 + it.path.hashCode() + it.lastModified + it.size }

            // Emit immediately, then poll for changes.
            var current = snapshot()
            lastSignature = signature(current)
            trySend(current)

            val watcher = launch(io) {
                while (isActive) {
                    kotlinx.coroutines.delay(1500)
                    val next = snapshot()
                    val sig = signature(next)
                    if (sig != lastSignature) {
                        lastSignature = sig
                        trySend(next)
                    }
                }
            }
            awaitClose { watcher.cancel() }
        }.flowOn(io)

    override suspend fun listDirectory(path: String, showHidden: Boolean): List<FileItem> =
        withContext(io) { local.list(path, showHidden) }

    override suspend fun getFile(path: String): FileItem? =
        withContext(io) { local.getItem(path) }

    override suspend fun getMountedVolumes(): List<StorageVolumeInfo> =
        withContext(io) { storage.getVolumes() }

    override suspend fun computeSize(path: String): Long =
        withContext(io) { local.computeSize(path) }

    override suspend fun createFolder(parentPath: String, name: String): OperationResult =
        withContext(io) {
            runCatching {
                val dir = local.mkdirs(parentPath, name)
                if (!dir.isDirectory) error("Could not create folder")
                OperationResult.Success(listOf(dir.absolutePath))
            }.getOrElse { OperationResult.Failure(it.message ?: "Create failed", it) }
        }

    override suspend fun createFile(parentPath: String, name: String): OperationResult =
        withContext(io) {
            runCatching {
                val target = File(parentPath, name)
                if (target.exists()) error("A file with that name already exists")
                val file = local.createNewFile(parentPath, name)
                OperationResult.Success(listOf(file.absolutePath))
            }.getOrElse { OperationResult.Failure(it.message ?: "Create failed", it) }
        }

    override suspend fun rename(path: String, newName: String): OperationResult =
        withContext(io) {
            runCatching {
                val target = File(File(path).parentFile, newName)
                if (target.exists()) error("A file with that name already exists")
                if (!local.rename(path, newName)) error("Rename failed")
                OperationResult.Success(listOf(target.absolutePath))
            }.getOrElse { OperationResult.Failure(it.message ?: "Rename failed", it) }
        }

    override fun copy(sources: List<String>, destinationDir: String): Flow<OperationResult> =
        transfer(sources, destinationDir, move = false)

    override fun move(sources: List<String>, destinationDir: String): Flow<OperationResult> =
        transfer(sources, destinationDir, move = true)

    private fun transfer(
        sources: List<String>,
        destinationDir: String,
        move: Boolean,
    ): Flow<OperationResult> = channelFlow {
        val roots = sources.map(::File).filter { it.exists() }
        val allFiles = roots.flatMap { root ->
            if (root.isDirectory) root.walkTopDown().filter { it.isFile }.toList()
            else listOf(root)
        }
        val totalBytes = allFiles.sumOf { it.length() }
        val totalCount = allFiles.size
        var processedCount = 0
        var processedBytes = 0L
        val affected = mutableListOf<String>()

        try {
            for (root in roots) {
                currentCoroutineContext().ensureActive()
                val destRoot = uniqueDestination(File(destinationDir), root.name)

                if (move && root.renameTo(destRoot)) {
                    // Fast path: same-filesystem rename moves the whole subtree.
                    val moved = if (destRoot.isDirectory) {
                        destRoot.walkTopDown().count { it.isFile }
                    } else 1
                    processedCount += moved
                    processedBytes = allFiles.take(processedCount).sumOf { it.length() }
                    affected += destRoot.absolutePath
                    send(OperationResult.Progress(processedCount, totalCount, root.name, processedBytes, totalBytes))
                    continue
                }

                if (root.isDirectory) {
                    root.walkTopDown().forEach { src ->
                        currentCoroutineContext().ensureActive()
                        val rel = src.toRelativeString(root.parentFile ?: root)
                        val dest = File(destinationDir, rel)
                        if (src.isDirectory) {
                            dest.mkdirs()
                        } else {
                            copyFile(src, dest)
                            processedCount++
                            processedBytes += src.length()
                            send(OperationResult.Progress(processedCount, totalCount, src.name, processedBytes, totalBytes))
                        }
                    }
                    if (move) root.deleteRecursively()
                } else {
                    copyFile(root, destRoot)
                    processedCount++
                    processedBytes += root.length()
                    send(OperationResult.Progress(processedCount, totalCount, root.name, processedBytes, totalBytes))
                    if (move) root.delete()
                }
                affected += destRoot.absolutePath
            }
            send(OperationResult.Success(affected))
        } catch (e: Exception) {
            send(OperationResult.Failure(e.message ?: "Operation failed", e))
        }
    }.flowOn(io)

    @Throws(IOException::class)
    private fun copyFile(src: File, dest: File) {
        dest.parentFile?.mkdirs()
        src.inputStream().use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output, bufferSize = 128 * 1024)
            }
        }
        dest.setLastModified(src.lastModified())
    }

    /** Avoids clobbering an existing target by appending " (n)". */
    private fun uniqueDestination(dir: File, name: String): File {
        var candidate = File(dir, name)
        if (!candidate.exists()) return candidate
        val base = name.substringBeforeLast('.', name)
        val ext = name.substringAfterLast('.', "")
        var i = 1
        while (candidate.exists()) {
            val newName = if (ext.isEmpty()) "$base ($i)" else "$base ($i).$ext"
            candidate = File(dir, newName)
            i++
        }
        return candidate
    }

    override suspend fun search(query: SearchQuery): Flow<List<FileItem>> = flow {
        val root = File(query.rootPath ?: android.os.Environment.getExternalStorageDirectory().absolutePath)
        val results = mutableListOf<FileItem>()
        val batch = ArrayDeque<File>()
        batch.add(root)
        var sinceEmit = 0
        while (batch.isNotEmpty()) {
            currentCoroutineContext().ensureActive()
            val dir = batch.removeFirst()
            val children = dir.listFiles() ?: continue
            for (child in children) {
                if (child.isDirectory) batch.add(child)
                val item = local.toFileItem(child)
                if (query.matches(item)) {
                    results.add(item)
                    sinceEmit++
                }
            }
            // Emit incrementally so the UI updates as the crawl proceeds.
            if (sinceEmit >= 20) {
                emit(results.toList())
                sinceEmit = 0
            }
        }
        emit(results.toList())
    }.flowOn(io)

    override fun readText(path: String): Flow<Result<String>> = flow {
        val result = runCatching {
            val file = File(path)
            if (file.length() > MAX_TEXT_BYTES) {
                val buffer = ByteArray(MAX_TEXT_BYTES.toInt())
                val read = file.inputStream().use { input ->
                    var off = 0
                    while (off < buffer.size) {
                        val n = input.read(buffer, off, buffer.size - off)
                        if (n < 0) break
                        off += n
                    }
                    off
                }
                buffer.decodeToString(0, read)
            } else {
                file.readText()
            }
        }
        emit(result)
    }.flowOn(io)

    override suspend fun writeText(path: String, content: String): OperationResult =
        withContext(io) {
            runCatching {
                File(path).writeText(content)
                OperationResult.Success(listOf(path))
            }.getOrElse { OperationResult.Failure(it.message ?: "Write failed", it) }
        }

    override suspend fun readBytes(path: String, offset: Long, length: Int): ByteArray =
        withContext(io) {
            File(path).inputStream().use { stream ->
                // skip() may return early; loop until the offset is reached.
                var remaining = offset
                while (remaining > 0) {
                    val skipped = stream.skip(remaining)
                    if (skipped <= 0) break
                    remaining -= skipped
                }
                val buffer = ByteArray(length)
                var off = 0
                while (off < length) {
                    val n = stream.read(buffer, off, length - off)
                    if (n < 0) break
                    off += n
                }
                if (off == length) buffer else buffer.copyOf(off)
            }
        }

    private companion object {
        const val MAX_TEXT_BYTES = 5L * 1024 * 1024
    }
}
