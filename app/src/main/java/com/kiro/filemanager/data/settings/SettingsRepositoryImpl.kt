package com.kiro.filemanager.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kiro.filemanager.domain.model.AppSettings
import com.kiro.filemanager.domain.model.SortField
import com.kiro.filemanager.domain.model.ThemeMode
import com.kiro.filemanager.domain.model.ViewMode
import com.kiro.filemanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val DYNAMIC = booleanPreferencesKey("dynamic_color")
        val ACCENT = longPreferencesKey("accent_argb")
        val DEFAULT_FOLDER = stringPreferencesKey("default_folder")
        val SHOW_HIDDEN = booleanPreferencesKey("show_hidden")
        val VIEW_MODE = stringPreferencesKey("view_mode")
        val SORT_FIELD = stringPreferencesKey("sort_field")
        val SORT_ASC = booleanPreferencesKey("sort_ascending")
        val FOLDERS_FIRST = booleanPreferencesKey("folders_first")
        val GRID_DP = intPreferencesKey("grid_thumbnail_dp")
        val LANGUAGE = stringPreferencesKey("language")
        val BIOMETRIC_VAULT = booleanPreferencesKey("biometric_vault")
    }

    override val settings: Flow<AppSettings> = dataStore.data.map { p ->
        AppSettings(
            themeMode = p[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            dynamicColor = p[Keys.DYNAMIC] ?: true,
            accentColorArgb = p[Keys.ACCENT] ?: 0xFF6750A4,
            defaultFolder = p[Keys.DEFAULT_FOLDER],
            showHidden = p[Keys.SHOW_HIDDEN] ?: false,
            viewMode = p[Keys.VIEW_MODE]?.let { runCatching { ViewMode.valueOf(it) }.getOrNull() }
                ?: ViewMode.LIST,
            sortField = p[Keys.SORT_FIELD]?.let { runCatching { SortField.valueOf(it) }.getOrNull() }
                ?: SortField.NAME,
            sortAscending = p[Keys.SORT_ASC] ?: true,
            foldersFirst = p[Keys.FOLDERS_FIRST] ?: true,
            gridThumbnailDp = p[Keys.GRID_DP] ?: 96,
            language = p[Keys.LANGUAGE] ?: "system",
            biometricVaultLock = p[Keys.BIOMETRIC_VAULT] ?: false,
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) =
        edit { it[Keys.THEME] = mode.name }

    override suspend fun setDynamicColor(enabled: Boolean) =
        edit { it[Keys.DYNAMIC] = enabled }

    override suspend fun setAccentColor(argb: Long) =
        edit { it[Keys.ACCENT] = argb }

    override suspend fun setShowHidden(show: Boolean) =
        edit { it[Keys.SHOW_HIDDEN] = show }

    override suspend fun setViewMode(mode: ViewMode) =
        edit { it[Keys.VIEW_MODE] = mode.name }

    override suspend fun setSort(field: SortField, ascending: Boolean) =
        edit { it[Keys.SORT_FIELD] = field.name; it[Keys.SORT_ASC] = ascending }

    override suspend fun setFoldersFirst(enabled: Boolean) =
        edit { it[Keys.FOLDERS_FIRST] = enabled }

    override suspend fun setDefaultFolder(path: String?) =
        edit { if (path == null) it.remove(Keys.DEFAULT_FOLDER) else it[Keys.DEFAULT_FOLDER] = path }

    override suspend fun setGridThumbnailDp(dp: Int) =
        edit { it[Keys.GRID_DP] = dp }

    override suspend fun setLanguage(language: String) =
        edit { it[Keys.LANGUAGE] = language }

    override suspend fun setBiometricVaultLock(enabled: Boolean) =
        edit { it[Keys.BIOMETRIC_VAULT] = enabled }

    private suspend inline fun edit(crossinline block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        dataStore.edit { block(it) }
    }
}
