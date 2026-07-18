package com.kiro.filemanager.domain.usecase

import com.kiro.filemanager.domain.model.CategorySummary
import com.kiro.filemanager.domain.model.FileCategory
import com.kiro.filemanager.domain.model.FileItem
import com.kiro.filemanager.domain.model.SearchFilter
import com.kiro.filemanager.domain.model.SearchQuery
import com.kiro.filemanager.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Lists every file belonging to a category across the primary storage tree.
 * Backed by the same recursive search infra as the search screen.
 */
class GetCategoryFilesUseCase @Inject constructor(
    private val fileRepository: FileRepository,
) {
    suspend operator fun invoke(category: FileCategory, rootPath: String): Flow<List<FileItem>> {
        val filter = when (category) {
            FileCategory.IMAGES -> SearchFilter.IMAGES
            FileCategory.VIDEOS -> SearchFilter.VIDEOS
            FileCategory.AUDIO -> SearchFilter.MUSIC
            FileCategory.APK -> SearchFilter.APK
            FileCategory.ARCHIVES -> SearchFilter.ARCHIVES
            FileCategory.DOCUMENTS -> SearchFilter.DOCUMENTS
        }
        return fileRepository.search(SearchQuery(filter = filter, rootPath = rootPath))
    }
}

/** Aggregates counts/sizes for the dashboard category cards. */
class GetCategorySummariesUseCase @Inject constructor(
    private val fileRepository: FileRepository,
) {
    suspend operator fun invoke(rootPath: String): Flow<List<CategorySummary>> {
        return fileRepository.search(SearchQuery(filter = SearchFilter.ALL, rootPath = rootPath))
            .map { items ->
                FileCategory.entries.map { category ->
                    val matching = items.filter { category.matches(it) }
                    CategorySummary(category, matching.size, matching.sumOf { it.size })
                }
            }
    }
}
