package com.kiro.filemanager.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiro.filemanager.domain.model.CategorySummary
import com.kiro.filemanager.domain.model.FileItem
import com.kiro.filemanager.domain.model.StorageVolumeInfo
import com.kiro.filemanager.domain.usecase.GetStorageVolumesUseCase
import com.kiro.filemanager.domain.usecase.ObserveFavoritesUseCase
import com.kiro.filemanager.domain.usecase.ObserveRecentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val volumes: List<StorageVolumeInfo> = emptyList(),
    val categories: List<CategorySummary> = emptyList(),
    val favorites: List<FileItem> = emptyList(),
    val recent: List<FileItem> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getStorageVolumes: GetStorageVolumesUseCase,
    observeFavorites: ObserveFavoritesUseCase,
    observeRecent: ObserveRecentUseCase,
) : ViewModel() {

    private val volumes = MutableStateFlow<List<StorageVolumeInfo>>(emptyList())

    val uiState: StateFlow<HomeUiState> =
        combine(
            volumes,
            observeFavorites(),
            observeRecent(20),
        ) { vols, favs, recent ->
            HomeUiState(
                volumes = vols,
                favorites = favs,
                recent = recent,
                isLoading = false,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    init {
        refreshVolumes()
    }

    fun refreshVolumes() {
        viewModelScope.launch {
            volumes.value = getStorageVolumes()
        }
    }
}
