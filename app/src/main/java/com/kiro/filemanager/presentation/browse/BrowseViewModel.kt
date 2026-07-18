package com.kiro.filemanager.presentation.browse

import android.os.Environment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiro.filemanager.domain.model.FileItem
import com.kiro.filemanager.domain.model.OperationResult
import com.kiro.filemanager.domain.model.SortField
import com.kiro.filemanager.domain.model.SortOrder
import com.kiro.filemanager.domain.model.ViewMode
import com.kiro.filemanager.domain.repository.SettingsRepository
import com.kiro.filemanager.domain.usecase.CopyUseCase
import com.kiro.filemanager.domain.usecase.CreateFileUseCase
import com.kiro.filemanager.domain.usecase.CreateFolderUseCase
import com.kiro.filemanager.domain.usecase.DeleteUseCase
import com.kiro.filemanager.domain.usecase.MoveUseCase
import com.kiro.filemanager.domain.usecase.ObserveDirectoryUseCase
import com.kiro.filemanager.domain.usecase.RecordAccessUseCase
import com.kiro.filemanager.domain.usecase.RenameUseCase
import com.kiro.filemanager.domain.usecase.ToggleFavoriteUseCase
import com.kiro.filemanager.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Pending clipboard operation for copy/move (cut) between directories. */
data class Clipboard(val paths: List<String>, val isMove: Boolean)

data class BrowseUiState(
    val path: String,
    val items: List<FileItem> = emptyList(),
    val isLoading: Boolean = true,
    val viewMode: ViewMode = ViewMode.LIST,
    val sortOrder: SortOrder = SortOrder(),
    val selection: Set<String> = emptySet(),
    val clipboard: Clipboard? = null,
    val gridThumbnailDp: Int = 96,
    val message: String? = null,
    val progress: Float? = null,
) {
    val inSelectionMode: Boolean get() = selection.isNotEmpty()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BrowseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeDirectory: ObserveDirectoryUseCase,
    private val settingsRepository: SettingsRepository,
    private val copyUseCase: CopyUseCase,
    private val moveUseCase: MoveUseCase,
    private val renameUseCase: RenameUseCase,
    private val createFolderUseCase: CreateFolderUseCase,
    private val createFileUseCase: CreateFileUseCase,
    private val deleteUseCase: DeleteUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val recordAccessUseCase: RecordAccessUseCase,
) : ViewModel() {

    val path: String = savedStateHandle.get<String>(Routes.BROWSE_ARG_PATH)
        ?.takeIf { it.isNotBlank() }
        ?: Environment.getExternalStorageDirectory().absolutePath

    private val transient = MutableStateFlow(
        TransientState(selection = emptySet(), clipboard = null, message = null, progress = null),
    )

    private data class TransientState(
        val selection: Set<String>,
        val clipboard: Clipboard?,
        val message: String?,
        val progress: Float?,
    )

    val uiState: StateFlow<BrowseUiState> =
        settingsRepository.settings
            .flatMapLatest { settings ->
                val order = settings.sortOrder
                combine(
                    observeDirectory(path, settings.showHidden, order),
                    transient,
                ) { items, t ->
                    BrowseUiState(
                        path = path,
                        items = items,
                        isLoading = false,
                        viewMode = settings.viewMode,
                        sortOrder = order,
                        selection = t.selection,
                        clipboard = t.clipboard,
                        gridThumbnailDp = settings.gridThumbnailDp,
                        message = t.message,
                        progress = t.progress,
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = BrowseUiState(path = path),
            )

    fun onItemOpened(item: FileItem) {
        viewModelScope.launch { recordAccessUseCase(item) }
    }

    fun toggleSelection(path: String) {
        transient.update { t ->
            val next = if (path in t.selection) t.selection - path else t.selection + path
            t.copy(selection = next)
        }
    }

    fun selectAll() {
        transient.update { it.copy(selection = uiState.value.items.map { i -> i.path }.toSet()) }
    }

    fun clearSelection() {
        transient.update { it.copy(selection = emptySet()) }
    }

    fun setViewMode(mode: ViewMode) = viewModelScope.launch { settingsRepository.setViewMode(mode) }

    fun setSort(field: SortField, ascending: Boolean) =
        viewModelScope.launch { settingsRepository.setSort(field, ascending) }

    fun toggleFavorite(item: FileItem) = viewModelScope.launch { toggleFavoriteUseCase(item) }

    fun copySelectionToClipboard(move: Boolean) {
        val sel = transient.value.selection
        if (sel.isEmpty()) return
        transient.update { it.copy(clipboard = Clipboard(sel.toList(), move), selection = emptySet()) }
    }

    fun paste() {
        val clip = transient.value.clipboard ?: return
        val flow = if (clip.isMove) moveUseCase(clip.paths, path) else copyUseCase(clip.paths, path)
        flow.onEach { result ->
            when (result) {
                is OperationResult.Progress -> transient.update { it.copy(progress = result.fraction) }
                is OperationResult.Success -> transient.update {
                    it.copy(progress = null, clipboard = null, message = "Done")
                }
                is OperationResult.Failure -> transient.update {
                    it.copy(progress = null, message = result.message)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun delete(paths: List<String>) {
        if (paths.isEmpty()) return
        deleteUseCase(paths).onEach { result ->
            when (result) {
                is OperationResult.Progress -> transient.update { it.copy(progress = result.fraction) }
                is OperationResult.Success -> transient.update {
                    it.copy(progress = null, selection = emptySet(), message = "Moved to Recycle Bin")
                }
                is OperationResult.Failure -> transient.update {
                    it.copy(progress = null, message = result.message)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun rename(path: String, newName: String) = viewModelScope.launch {
        val result = renameUseCase(path, newName)
        if (result is OperationResult.Failure) transient.update { it.copy(message = result.message) }
    }

    fun createFolder(name: String) = viewModelScope.launch {
        val result = createFolderUseCase(path, name)
        if (result is OperationResult.Failure) transient.update { it.copy(message = result.message) }
    }

    fun createFile(name: String) = viewModelScope.launch {
        val result = createFileUseCase(path, name)
        if (result is OperationResult.Failure) transient.update { it.copy(message = result.message) }
    }

    fun consumeMessage() = transient.update { it.copy(message = null) }
}
