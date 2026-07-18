package com.kiro.filemanager.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Sd
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kiro.filemanager.domain.model.FileCategory
import com.kiro.filemanager.domain.model.StorageType
import com.kiro.filemanager.domain.model.StorageVolumeInfo
import com.kiro.filemanager.presentation.util.formatBytes

@Composable
fun HomeScreen(
    onOpenPath: (String) -> Unit,
    onOpenCategory: (FileCategory) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        item {
            Text(
                "Storage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        items(state.volumes) { volume ->
            StorageCard(volume, onClick = { onOpenPath(volume.path) })
        }

        item {
            Text(
                "Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FileCategory.entries.chunked(2).forEach { rowCats ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowCats.forEach { cat ->
                            CategoryChip(
                                category = cat,
                                modifier = Modifier.weight(1f),
                                onClick = { onOpenCategory(cat) },
                            )
                        }
                        if (rowCats.size == 1) Box(Modifier.weight(1f)) {}
                    }
                }
            }
        }

        if (state.recent.isNotEmpty()) {
            item {
                Text(
                    "Recent",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(state.recent) { item ->
                Surface(
                    onClick = { onOpenPath(item.path) },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            item.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                        )
                        Text(
                            formatBytes(item.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageCard(volume: StorageVolumeInfo, onClick: () -> Unit) {
    val icon = when (volume.type) {
        StorageType.INTERNAL -> Icons.Filled.Smartphone
        StorageType.SD_CARD -> Icons.Filled.Sd
        StorageType.USB_OTG -> Icons.Filled.Usb
    }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    volume.label,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 12.dp).weight(1f),
                )
                Text(
                    "${formatBytes(volume.usedBytes)} / ${formatBytes(volume.totalBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LinearProgressIndicator(
                progress = { volume.usedFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: FileCategory,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val (icon, label) = when (category) {
        FileCategory.IMAGES -> Icons.Filled.Image to "Images"
        FileCategory.VIDEOS -> Icons.Filled.Movie to "Videos"
        FileCategory.AUDIO -> Icons.Filled.AudioFile to "Audio"
        FileCategory.DOCUMENTS -> Icons.Filled.Description to "Documents"
        FileCategory.APK -> Icons.Filled.Android to "APKs"
        FileCategory.ARCHIVES -> Icons.Filled.Archive to "Archives"
    }
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 10.dp))
        }
    }
}
