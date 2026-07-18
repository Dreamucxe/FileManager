package com.kiro.filemanager.presentation.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kiro.filemanager.domain.model.FileItem
import com.kiro.filemanager.domain.model.SearchFilter
import com.kiro.filemanager.presentation.components.FileListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onOpenFolder: (String) -> Unit,
    onOpenFile: (FileItem) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            placeholder = { Text("Search files…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SearchFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.filter == filter,
                    onClick = { viewModel.onFilterChange(filter) },
                    label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                )
            }
        }

        Box(Modifier.fillMaxSize()) {
            when {
                state.isSearching -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.results.isEmpty() && (state.query.isNotBlank() || state.filter != SearchFilter.ALL) ->
                    Text(
                        "No results found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center),
                    )
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(state.results, key = { it.path }) { item ->
                        FileListItem(
                            item = item,
                            selected = false,
                            onClick = { if (item.isDirectory) onOpenFolder(item.path) else onOpenFile(item) },
                            onLongClick = {},
                        )
                    }
                }
            }
        }
    }
}
