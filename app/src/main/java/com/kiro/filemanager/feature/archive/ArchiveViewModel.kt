package com.kiro.filemanager.feature.archive

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiro.filemanager.domain.model.ArchiveEntry
import com.kiro.filemanager.domain.model.OperationResult
import com.kiro.filemanager.domain.repository.ArchiveRepository
import com.kiro.filemanager.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ArchiveUiState(
    val fileName: String = "",
    val entries: List<ArchiveEntry> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val extractProgress: Float? = null,
    val message: String? = null,
)

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val archiveRepository: ArchiveRepository,
) : ViewModel() {

    private val archivePath: String = savedStateHandle.get<String>(Routes.ARCHIVE_VIEWER_ARG_PATH).orEmpty()

    private val _state = MutableStateFlow(ArchiveUiState(fileName = File(archivePath).name))
    val state: StateFlow<ArchiveUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            archiveRepository.listEntries(archivePath)
                .onSuccess { entries -> _state.update { it.copy(entries = entries, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message ?: "Cannot read archive") } }
        }
    }

    /** Extracts alongside the archive, into a folder named after it. */
    fun extract() {
        val parent = File(archivePath).parent ?: return
        val destName = File(archivePath).nameWithoutExtension
        val dest = File(parent, destName).absolutePath
        archiveRepository.extract(archivePath, dest).onEach { result ->
            when (result) {
                is OperationResult.Progress -> _state.update { it.copy(extractProgress = result.fraction) }
                is OperationResult.Success -> _state.update { it.copy(extractProgress = null, message = "Extracted to $destName") }
                is OperationResult.Failure -> _state.update { it.copy(extractProgress = null, message = result.message) }
            }
        }.launchIn(viewModelScope)
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }
}
