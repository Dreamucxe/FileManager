package com.kiro.filemanager.domain.repository

import com.kiro.filemanager.domain.model.AppSettings
import com.kiro.filemanager.domain.model.SortField
import com.kiro.filemanager.domain.model.ThemeMode
import com.kiro.filemanager.domain.model.ViewMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setAccentColor(argb: Long)
    suspend fun setShowHidden(show: Boolean)
    suspend fun setViewMode(mode: ViewMode)
    suspend fun setSort(field: SortField, ascending: Boolean)
    suspend fun setFoldersFirst(enabled: Boolean)
    suspend fun setDefaultFolder(path: String?)
    suspend fun setGridThumbnailDp(dp: Int)
    suspend fun setLanguage(language: String)
    suspend fun setBiometricVaultLock(enabled: Boolean)
}
