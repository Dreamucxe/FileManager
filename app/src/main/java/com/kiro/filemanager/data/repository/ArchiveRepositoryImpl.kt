package com.kiro.filemanager.data.repository

import com.kiro.filemanager.core.util.IoDispatcher
import com.kiro.filemanager.domain.model.ArchiveEntry
import com.kiro.filemanager.domain.model.OperationResult
import com.kiro.filemanager.domain.repository.ArchiveRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ZIP support via java.util.zip; TAR/TAR.GZ inspection+extraction via
 * commons-compress. Creating archives currently targets ZIP.
 */
@Singleton
class ArchiveRepositoryImpl @Inject constructor(
    @IoDispatcher private val io: CoroutineDispatcher,
) : ArchiveRepository {

    private val supported = setOf("zip", "jar", "tar", "gz", "tgz")

    override fun isSupported(extension: String): Boolean = extension.lowercase() in supported

    override suspend fun listEntries(archivePath: String): Result<List<ArchiveEntry>> =
        withContext(io) {
            runCatching {
                val file = File(archivePath)
                when (file.extension.lowercase()) {
                    "zip", "jar" -> listZip(file)
                    "tar" -> listTar(FileInputStream(file))
                    "gz", "tgz" -> {
                        if (file.name.endsWith(".tar.gz") || file.extension.equals("tgz", true)) {
                            listTar(GzipCompressorInputStream(BufferedInputStream(FileInputStream(file))))
                        } else emptyList()
                    }
                    else -> error("Unsupported archive type")
                }
            }
        }

    private fun listZip(file: File): List<ArchiveEntry> {
        val entries = mutableListOf<ArchiveEntry>()
        ZipFile(file).use { zip ->
            val e = zip.entries()
            while (e.hasMoreElements()) {
                val entry = e.nextElement()
                entries += ArchiveEntry(
                    name = entry.name,
                    isDirectory = entry.isDirectory,
                    size = entry.size.coerceAtLeast(0),
                    compressedSize = entry.compressedSize.coerceAtLeast(0),
                    lastModified = entry.time,
                )
            }
        }
        return entries
    }

    private fun listTar(input: java.io.InputStream): List<ArchiveEntry> {
        val entries = mutableListOf<ArchiveEntry>()
        TarArchiveInputStream(input).use { tar ->
            var entry = tar.nextTarEntry
            while (entry != null) {
                entries += ArchiveEntry(
                    name = entry.name,
                    isDirectory = entry.isDirectory,
                    size = entry.size,
                    compressedSize = entry.size,
                    lastModified = entry.modTime.time,
                )
                entry = tar.nextTarEntry
            }
        }
        return entries
    }

    override fun createZip(sources: List<String>, destinationZip: String): Flow<OperationResult> =
        channelFlow {
            val files = sources.map(::File).flatMap { root ->
                if (root.isDirectory) root.walkTopDown().filter { it.isFile }.map { root.parentFile to it }.toList()
                else listOf(root.parentFile to root)
            }
            val total = files.size
            var processed = 0
            try {
                ZipOutputStream(File(destinationZip).outputStream().buffered()).use { zos ->
                    for ((base, file) in files) {
                        currentCoroutineContext().ensureActive()
                        val entryName = file.toRelativeString(base ?: file.parentFile ?: file)
                        zos.putNextEntry(ZipEntry(entryName))
                        file.inputStream().use { it.copyTo(zos, 128 * 1024) }
                        zos.closeEntry()
                        processed++
                        send(OperationResult.Progress(processed, total, file.name))
                    }
                }
                send(OperationResult.Success(listOf(destinationZip)))
            } catch (e: Exception) {
                send(OperationResult.Failure(e.message ?: "Compression failed", e))
            }
        }.flowOn(io)

    override fun extract(archivePath: String, destinationDir: String): Flow<OperationResult> =
        channelFlow {
            val file = File(archivePath)
            val outDir = File(destinationDir).apply { mkdirs() }
            try {
                when (file.extension.lowercase()) {
                    "zip", "jar" -> extractZip(file, outDir) { p, t, n ->
                        trySend(OperationResult.Progress(p, t, n))
                    }
                    "tar" -> extractTar(FileInputStream(file), outDir) { n ->
                        trySend(OperationResult.Progress(0, 0, n))
                    }
                    "gz", "tgz" -> extractTar(
                        GzipCompressorInputStream(BufferedInputStream(FileInputStream(file))), outDir
                    ) { n -> trySend(OperationResult.Progress(0, 0, n)) }
                    else -> error("Unsupported archive type")
                }
                send(OperationResult.Success(listOf(outDir.absolutePath)))
            } catch (e: Exception) {
                send(OperationResult.Failure(e.message ?: "Extraction failed", e))
            }
        }.flowOn(io)

    private suspend fun extractZip(
        file: File,
        outDir: File,
        onProgress: (Int, Int, String) -> Unit,
    ) {
        val total = ZipFile(file).use { it.size() }
        var processed = 0
        ZipInputStream(file.inputStream().buffered()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                currentCoroutineContext().ensureActive()
                val target = safeResolve(outDir, entry.name)
                if (entry.isDirectory) {
                    target.mkdirs()
                } else {
                    target.parentFile?.mkdirs()
                    target.outputStream().use { zis.copyTo(it, 128 * 1024) }
                }
                processed++
                onProgress(processed, total, entry.name)
                entry = zis.nextEntry
            }
        }
    }

    private suspend fun extractTar(
        input: java.io.InputStream,
        outDir: File,
        onProgress: (String) -> Unit,
    ) {
        TarArchiveInputStream(input).use { tar ->
            var entry = tar.nextTarEntry
            while (entry != null) {
                currentCoroutineContext().ensureActive()
                val target = safeResolve(outDir, entry.name)
                if (entry.isDirectory) {
                    target.mkdirs()
                } else {
                    target.parentFile?.mkdirs()
                    target.outputStream().use { tar.copyTo(it, 128 * 1024) }
                }
                onProgress(entry.name)
                entry = tar.nextTarEntry
            }
        }
    }

    /** Guards against Zip Slip path traversal. */
    private fun safeResolve(baseDir: File, entryName: String): File {
        val target = File(baseDir, entryName)
        val canonicalBase = baseDir.canonicalPath
        val canonicalTarget = target.canonicalPath
        if (!canonicalTarget.startsWith(canonicalBase + File.separator) &&
            canonicalTarget != canonicalBase
        ) {
            throw SecurityException("Blocked path traversal entry: $entryName")
        }
        return target
    }
}
