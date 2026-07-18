package com.kiro.filemanager.domain.model

enum class SearchFilter {
    ALL, IMAGES, VIDEOS, MUSIC, APK, ARCHIVES, DOCUMENTS, FOLDERS, LARGE_FILES, RECENT
}

/**
 * A file-system search request. [useRegex] switches [text] from a substring
 * match to a regular expression matched against the file name.
 */
data class SearchQuery(
    val text: String = "",
    val filter: SearchFilter = SearchFilter.ALL,
    val useRegex: Boolean = false,
    val rootPath: String? = null,
    /** Bytes; entries below this are excluded when filter == LARGE_FILES. */
    val largeFileThreshold: Long = 100L * 1024 * 1024,
) {
    fun matches(item: FileItem): Boolean {
        if (!matchesFilter(item)) return false
        if (text.isBlank()) return true
        return if (useRegex) {
            runCatching { Regex(text, RegexOption.IGNORE_CASE).containsMatchIn(item.name) }
                .getOrDefault(false)
        } else {
            item.name.contains(text, ignoreCase = true)
        }
    }

    private fun matchesFilter(item: FileItem): Boolean = when (filter) {
        SearchFilter.ALL -> true
        SearchFilter.IMAGES -> item.type == FileType.IMAGE
        SearchFilter.VIDEOS -> item.type == FileType.VIDEO
        SearchFilter.MUSIC -> item.type == FileType.AUDIO
        SearchFilter.APK -> item.type == FileType.APK
        SearchFilter.ARCHIVES -> item.type == FileType.ARCHIVE
        SearchFilter.DOCUMENTS ->
            item.type == FileType.PDF || item.type == FileType.DOCUMENT ||
                item.type == FileType.TEXT || item.type == FileType.MARKDOWN
        SearchFilter.FOLDERS -> item.isDirectory
        SearchFilter.LARGE_FILES -> !item.isDirectory && item.size >= largeFileThreshold
        SearchFilter.RECENT ->
            System.currentTimeMillis() - item.lastModified < 7L * 24 * 60 * 60 * 1000
    }
}
