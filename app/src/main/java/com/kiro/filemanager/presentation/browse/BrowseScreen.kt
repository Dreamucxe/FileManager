package com.kiro.filemanager.presentation.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kiro.filemanager.domain.model.FileItem
import com.kiro.filemanager.domain.model.SortField
import com.kiro.filemanager.domain.model.ViewMode
import com.kiro.filemanager.presentation.components.ConfirmDialog
import com.kiro.filemanager.presentation.components.FileGridItem
import com.kiro.filemanager.presentation.components.FileListItem
import com.kiro.filemanager.presentation.components.InputDialog
import com.kiro.filemanager.presentation.util.openFileExternally
import com.kiro.filemanager.presentation.util.shareFiles
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    onNavigateToFolder: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onOpenTextFile: (String) -> Unit,
    onOpenApk: (String) -> Unit,
    onOpenArchive: (String) -> Unit,
    viewModel: BrowseViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }

    var showCreateFolder by remember { mutableStateOf(false) }
    var showCreateFile by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<FileItem?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHost.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    fun onItemClick(item: FileItem) {
        if (state.inSelectionMode) {
            viewModel.toggleSelection(item.path)
            return
        }
        if (item.isDirectory) {
            onNavigateToFolder(item.path)
        } else {
            viewModel.onItemOpened(item)
            when (item.type) {
                com.kiro.filemanager.domain.model.FileType.APK -> onOpenApk(item.path)
                com.kiro.filemanager.domain.model.FileType.ARCHIVE -> onOpenArchive(item.path)
                else -> if (item.type.isTextEditable) onOpenTextFile(item.path)
                else context.openFileExternally(item.path)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            if (state.inSelectionMode) {
                TopAppBar(
                    title = { Text("${state.selection.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = viewModel::clearSelection) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.copySelectionToClipboard(move = false) }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                        }
                        IconButton(onClick = { viewModel.copySelectionToClipboard(move = true) }) {
                            Icon(Icons.Filled.ContentCut, contentDescription = "Cut")
                        }
                        IconButton(onClick = { context.shareFiles(state.selection.toList()) }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    },
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = File(state.path).name.ifEmpty { state.path },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Up")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.setViewMode(
                                if (state.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST,
                            )
                        }) {
                            Icon(
                                if (state.viewMode == ViewMode.LIST) Icons.Filled.GridView
                                else Icons.AutoMirrored.Filled.ViewList,
                                contentDescription = "Toggle view",
                            )
                        }
                        Box {
                            IconButton(onClick = { sortExpanded = true }) {
                                Icon(Icons.Filled.Sort, contentDescription = "Sort")
                            }
                            DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                                SortField.entries.forEach { field ->
                                    DropdownMenuItem(
                                        text = { Text(field.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                        onClick = {
                                            val asc = if (state.sortOrder.field == field) !state.sortOrder.ascending else true
                                            viewModel.setSort(field, asc)
                                            sortExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "More")
                            }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                if (state.clipboard != null) {
                                    DropdownMenuItem(
                                        text = { Text("Paste (${state.clipboard!!.paths.size})") },
                                        leadingIcon = { Icon(Icons.Filled.ContentPaste, null) },
                                        onClick = { viewModel.paste(); menuExpanded = false },
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Select all") },
                                    onClick = { viewModel.selectAll(); menuExpanded = false },
                                )
                            }
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            if (!state.inSelectionMode) {
                Box {
                    ExtendedFloatingActionButton(
                        text = { Text("New") },
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        onClick = { fabExpanded = true },
                    )
                    DropdownMenu(expanded = fabExpanded, onDismissRequest = { fabExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("New folder") },
                            leadingIcon = { Icon(Icons.Filled.CreateNewFolder, null) },
                            onClick = { showCreateFolder = true; fabExpanded = false },
                        )
                        DropdownMenuItem(
                            text = { Text("New file") },
                            leadingIcon = { Icon(Icons.Filled.Add, null) },
                            onClick = { showCreateFile = true; fabExpanded = false },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            state.progress?.let { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            if (state.items.isEmpty() && !state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("This folder is empty", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (state.viewMode == ViewMode.GRID) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(state.gridThumbnailDp.dp + 24.dp),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.items, key = { it.path }) { item ->
                        FileGridItem(
                            item = item,
                            selected = item.path in state.selection,
                            thumbnailDp = state.gridThumbnailDp,
                            onClick = { onItemClick(item) },
                            onLongClick = { viewModel.toggleSelection(item.path) },
                        )
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.items, key = { it.path }) { item ->
                        FileListItem(
                            item = item,
                            selected = item.path in state.selection,
                            onClick = { onItemClick(item) },
                            onLongClick = { viewModel.toggleSelection(item.path) },
                        )
                    }
                }
            }
        }
    }

    if (showCreateFolder) {
        InputDialog(
            title = "New folder",
            onConfirm = { viewModel.createFolder(it); showCreateFolder = false },
            onDismiss = { showCreateFolder = false },
        )
    }
    if (showCreateFile) {
        InputDialog(
            title = "New file",
            onConfirm = { viewModel.createFile(it); showCreateFile = false },
            onDismiss = { showCreateFile = false },
        )
    }
    renameTarget?.let { target ->
        InputDialog(
            title = "Rename",
            initialValue = target.name,
            onConfirm = { viewModel.rename(target.path, it); renameTarget = null },
            onDismiss = { renameTarget = null },
        )
    }
    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete",
            message = "Move ${state.selection.size} item(s) to the Recycle Bin?",
            confirmLabel = "Delete",
            onConfirm = { viewModel.delete(state.selection.toList()); showDeleteConfirm = false },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}
