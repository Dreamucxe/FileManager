package com.kiro.filemanager.feature.apk

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiro.filemanager.domain.model.ApkInfo
import com.kiro.filemanager.domain.repository.ApkRepository
import com.kiro.filemanager.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApkInfoUiState(
    val info: ApkInfo? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class ApkInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val apkRepository: ApkRepository,
) : ViewModel() {

    val apkPath: String = savedStateHandle.get<String>(Routes.APK_INFO_ARG_PATH).orEmpty()

    private val _state = MutableStateFlow(ApkInfoUiState())
    val state: StateFlow<ApkInfoUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            apkRepository.analyzeApk(apkPath)
                .onSuccess { info -> _state.update { it.copy(info = info, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message ?: "Cannot read APK") } }
        }
    }
}
