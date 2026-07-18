package com.kiro.filemanager.presentation.search

import android.os.Environment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiro.filemanager.domain.model.FileItem
import com.kiro.filemanager.domain.model.SearchFilter
import com.kiro.filemanager.domain.model.SearchQuery
import com.kiro.filemanager.domain.usecase.SearchUseCase
import com.kiro.filemanager.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val filter: SearchFilter = SearchFilter.ALL,
    val useRegex: Boolean = false,
    val results: List<FileItem> = emptyList(),
    val isSearching: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialFilter: SearchFilter =
        savedStateHandle.get<String>(Routes.SEARCH_ARG_FILTER)
            ?.let { name -> runCatching { SearchFilter.valueOf(name) }.getOrNull() }
            ?: SearchFilter.ALL

    private val input = MutableStateFlow(SearchUiState(filter = initialFilter))

    val uiState: StateFlow<SearchUiState> =
        input
            .debounce { if (it.query.isBlank()) 0 else 300 }
            .distinctUntilChanged { old, new ->
                old.query == new.query && old.filter == new.filter && old.useRegex == new.useRegex
            }
            .flatMapLatest { state ->
                if (state.query.isBlank() && state.filter == SearchFilter.ALL) {
                    flowOf(state.copy(results = emptyList(), isSearching = false))
                } else {
                    flow {
                        emit(state.copy(isSearching = true))
                        val query = SearchQuery(
                            text = state.query,
                            filter = state.filter,
                            useRegex = state.useRegex,
                            rootPath = Environment.getExternalStorageDirectory().absolutePath,
                        )
                        emitAll(
                            searchUseCase(query).let { resultsFlow ->
                                flow {
                                    resultsFlow.collect { results ->
                                        emit(state.copy(results = results, isSearching = false))
                                    }
                                }
                            },
                        )
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SearchUiState(filter = initialFilter),
            )

    fun onQueryChange(text: String) = input.update { it.copy(query = text) }
    fun onFilterChange(filter: SearchFilter) = input.update { it.copy(filter = filter) }
    fun onRegexToggle(enabled: Boolean) = input.update { it.copy(useRegex = enabled) }
}
