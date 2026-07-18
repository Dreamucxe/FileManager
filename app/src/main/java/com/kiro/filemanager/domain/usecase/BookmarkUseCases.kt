package com.kiro.filemanager.domain.usecase

import com.kiro.filemanager.domain.model.FileItem
import com.kiro.filemanager.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
) {
    suspend operator fun invoke(item: FileItem) {
        if (bookmarkRepository.isFavorite(item.path)) {
            bookmarkRepository.removeFavorite(item.path)
        } else {
            bookmarkRepository.addFavorite(item)
        }
    }
}

class ObserveFavoritesUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
) {
    operator fun invoke(): Flow<List<FileItem>> = bookmarkRepository.observeFavorites()
}

class ObserveRecentUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
) {
    operator fun invoke(limit: Int = 50): Flow<List<FileItem>> =
        bookmarkRepository.observeRecent(limit)
}

class RecordAccessUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
) {
    suspend operator fun invoke(item: FileItem) = bookmarkRepository.recordAccess(item)
}
