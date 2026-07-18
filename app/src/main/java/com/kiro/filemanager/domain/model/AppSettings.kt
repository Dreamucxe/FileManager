package com.kiro.filemanager.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK, AMOLED }

/**
 * User preferences persisted via DataStore. Immutable snapshot consumed by the UI.
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val accentColorArgb: Long = 0xFF6750A4,
    val defaultFolder: String? = null,
    val showHidden: Boolean = false,
    val viewMode: ViewMode = ViewMode.LIST,
    val sortField: SortField = SortField.NAME,
    val sortAscending: Boolean = true,
    val foldersFirst: Boolean = true,
    val gridThumbnailDp: Int = 96,
    val language: String = "system",
    val biometricVaultLock: Boolean = false,
) {
    val sortOrder: SortOrder
        get() = SortOrder(sortField, sortAscending, foldersFirst)
}
