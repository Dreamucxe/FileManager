package com.kiro.filemanager.presentation.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.ui.graphics.vector.ImageVector
import com.kiro.filemanager.domain.model.FileType

/** Maps a [FileType] to a Material icon for list/grid rendering. */
fun iconFor(type: FileType): ImageVector = when (type) {
    FileType.FOLDER -> Icons.Filled.Folder
    FileType.IMAGE -> Icons.Filled.Image
    FileType.VIDEO -> Icons.Filled.Movie
    FileType.AUDIO -> Icons.Filled.AudioFile
    FileType.PDF -> Icons.Filled.PictureAsPdf
    FileType.DOCUMENT -> Icons.Filled.Description
    FileType.TEXT, FileType.MARKDOWN -> Icons.Filled.TextSnippet
    FileType.ARCHIVE -> Icons.Filled.Archive
    FileType.APK -> Icons.Filled.Android
    FileType.CODE, FileType.JSON, FileType.XML, FileType.HTML -> Icons.Filled.Code
    FileType.BINARY, FileType.UNKNOWN -> Icons.AutoMirrored.Filled.InsertDriveFile
}
