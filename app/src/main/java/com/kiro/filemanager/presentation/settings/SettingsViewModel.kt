package com.kiro.filemanager.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiro.filemanager.domain.model.AppSettings
import com.kiro.filemanager.domain.model.SortField
import com.kiro.filemanager.domain.model.ThemeMode
import com.kiro.filemanager.domain.model.ViewMode
import com.kiro.filemanager.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch { settingsRepository.setDynamicColor(enabled) }
    fun setShowHidden(show: Boolean) = viewModelScope.launch { settingsRepository.setShowHidden(show) }
    fun setViewMode(mode: ViewMode) = viewModelScope.launch { settingsRepository.setViewMode(mode) }
    fun setSort(field: SortField, ascending: Boolean) = viewModelScope.launch { settingsRepository.setSort(field, ascending) }
    fun setFoldersFirst(enabled: Boolean) = viewModelScope.launch { settingsRepository.setFoldersFirst(enabled) }
    fun setGridThumbnailDp(dp: Int) = viewModelScope.launch { settingsRepository.setGridThumbnailDp(dp) }
    fun setBiometricVaultLock(enabled: Boolean) = viewModelScope.launch { settingsRepository.setBiometricVaultLock(enabled) }
    fun setDefaultFolder(path: String?) = viewModelScope.launch { settingsRepository.setDefaultFolder(path) }
}
