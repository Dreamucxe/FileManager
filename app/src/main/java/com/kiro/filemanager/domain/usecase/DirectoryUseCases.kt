package com.kiro.filemanager.domain.usecase

import com.kiro.filemanager.domain.model.FileItem
import com.kiro.filemanager.domain.model.SortOrder
import com.kiro.filemanager.domain.repository.BookmarkRepository
import com.kiro.filemanager.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Observes a directory, applies the favorite flag from the bookmark store, and
 * sorts according to the current [SortOrder]. This is the primary listing feed
 * for the browser screen.
 */
class ObserveDirectoryUseCase @Inject constructor(
    private val fileRepository: FileRepository,
    private val bookmarkRepository: BookmarkRepository,
) {
    operator fun invoke(
        path: String,
        showHidden: Boolean,
        sortOrder: SortOrder,
    ): Flow<List<FileItem>> =
        combine(
            fileRepository.observeDirectory(path, showHidden),
            bookmarkRepository.observeFavoritePaths(),
        ) { items, favorites ->
            items
                .map { it.copy(isFavorite = it.path in favorites) }
                .sortedWith(sortOrder.comparator())
        }
}

class GetFileUseCase @Inject constructor(
    private val fileRepository: FileRepository,
) {
    suspend operator fun invoke(path: String): FileItem? = fileRepository.getFile(path)
}

class ComputeFolderSizeUseCase @Inject constructor(
    private val fileRepository: FileRepository,
) {
    suspend operator fun invoke(path: String): Long = fileRepository.computeSize(path)
}

class GetStorageVolumesUseCase @Inject constructor(
    private val fileRepository: FileRepository,
) {
    suspend operator fun invoke() = fileRepository.getMountedVolumes()
}
