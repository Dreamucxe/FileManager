package com.kiro.filemanager.domain.model

/**
 * A single entry (file or directory) in the file system, independent of the
 * underlying access mechanism (java.io, SAF DocumentFile, MediaStore, Shizuku).
 */
data class FileItem(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val extension: String,
    val mimeType: String,
    val isHidden: Boolean,
    val canRead: Boolean = true,
    val canWrite: Boolean = true,
    /** Number of children for a directory, or -1 when not computed. */
    val childCount: Int = -1,
    val isFavorite: Boolean = false,
) {
    val type: FileType = if (isDirectory) FileType.FOLDER else FileType.fromExtension(extension)

    val parentPath: String?
        get() = path.substringBeforeLast('/', "").ifEmpty { null }

    val nameWithoutExtension: String
        get() = if (extension.isEmpty()) name else name.removeSuffix(".$extension")
}
