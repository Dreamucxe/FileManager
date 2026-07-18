package com.kiro.filemanager.domain.repository

import com.kiro.filemanager.domain.model.ArchiveEntry
import com.kiro.filemanager.domain.model.OperationResult
import kotlinx.coroutines.flow.Flow

/** Archive create / inspect / extract. ZIP and TAR/GZIP are natively supported. */
interface ArchiveRepository {
    /** Lists entries without extracting. Supports zip, jar, tar, tar.gz. */
    suspend fun listEntries(archivePath: String): Result<List<ArchiveEntry>>

    /** Compresses [sources] into a new zip at [destinationZip]. */
    fun createZip(sources: List<String>, destinationZip: String): Flow<OperationResult>

    /** Extracts an archive into [destinationDir]. */
    fun extract(archivePath: String, destinationDir: String): Flow<OperationResult>

    /** True when the extension is one this repository can read. */
    fun isSupported(extension: String): Boolean
}
