package com.kiro.filemanager.domain.model

enum class SortField { NAME, SIZE, DATE, TYPE }

/**
 * Sorting preference for a directory listing. Folders are grouped first by default.
 */
data class SortOrder(
    val field: SortField = SortField.NAME,
    val ascending: Boolean = true,
    val foldersFirst: Boolean = true,
) {
    /** Returns a comparator that applies this sort order to a list of [FileItem]. */
    fun comparator(): Comparator<FileItem> {
        val base = when (field) {
            SortField.NAME -> compareBy<FileItem> { it.name.lowercase() }
            SortField.SIZE -> compareBy { it.size }
            SortField.DATE -> compareBy { it.lastModified }
            SortField.TYPE -> compareBy<FileItem> { it.extension.lowercase() }
                .thenBy { it.name.lowercase() }
        }
        val directional = if (ascending) base else base.reversed()
        return if (foldersFirst) {
            compareByDescending<FileItem> { it.isDirectory }.then(directional)
        } else {
            directional
        }
    }
}
