package com.kiro.filemanager.feature.textviewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiro.filemanager.domain.model.OperationResult
import com.kiro.filemanager.domain.repository.FileRepository
import com.kiro.filemanager.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class TextViewerUiState(
    val fileName: String = "",
    val content: String = "",
    val isLoading: Boolean = true,
    val isDirty: Boolean = false,
    val error: String? = null,
    val savedMessage: String? = null,
)

@HiltViewModel
class TextViewerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val fileRepository: FileRepository,
) : ViewModel() {

    private val path: String = savedStateHandle.get<String>(Routes.TEXT_VIEWER_ARG_PATH).orEmpty()

    private val _state = MutableStateFlow(TextViewerUiState(fileName = File(path).name))
    val state: StateFlow<TextViewerUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            fileRepository.readText(path).collect { result ->
                result
                    .onSuccess { text -> _state.update { it.copy(content = text, isLoading = false) } }
                    .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message ?: "Cannot read file") } }
            }
        }
    }

    fun onContentChange(text: String) {
        _state.update { it.copy(content = text, isDirty = true) }
    }

    fun save() {
        viewModelScope.launch {
            when (val result = fileRepository.writeText(path, _state.value.content)) {
                is OperationResult.Success -> _state.update { it.copy(isDirty = false, savedMessage = "Saved") }
                is OperationResult.Failure -> _state.update { it.copy(error = result.message) }
                else -> Unit
            }
        }
    }

    fun consumeMessages() = _state.update { it.copy(savedMessage = null, error = null) }
}
