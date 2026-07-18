package com.kiro.filemanager.domain.model

/**
 * Dashboard categories that aggregate files across the whole device by type.
 */
enum class FileCategory(val types: Set<FileType>) {
    IMAGES(setOf(FileType.IMAGE)),
    VIDEOS(setOf(FileType.VIDEO)),
    AUDIO(setOf(FileType.AUDIO)),
    DOCUMENTS(setOf(FileType.PDF, FileType.DOCUMENT, FileType.TEXT, FileType.MARKDOWN)),
    APK(setOf(FileType.APK)),
    ARCHIVES(setOf(FileType.ARCHIVE));

    fun matches(item: FileItem): Boolean = !item.isDirectory && item.type in types

    /** The search filter that lists exactly this category's files. */
    fun toSearchFilter(): SearchFilter = when (this) {
        IMAGES -> SearchFilter.IMAGES
        VIDEOS -> SearchFilter.VIDEOS
        AUDIO -> SearchFilter.MUSIC
        DOCUMENTS -> SearchFilter.DOCUMENTS
        APK -> SearchFilter.APK
        ARCHIVES -> SearchFilter.ARCHIVES
    }
}

/**
 * Aggregate count + size for a category, shown on the dashboard cards.
 */
data class CategorySummary(
    val category: FileCategory,
    val count: Int,
    val totalBytes: Long,
)
