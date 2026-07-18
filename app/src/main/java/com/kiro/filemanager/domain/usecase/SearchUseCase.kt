package com.kiro.filemanager.domain.usecase

import com.kiro.filemanager.domain.model.FileItem
import com.kiro.filemanager.domain.model.SearchQuery
import com.kiro.filemanager.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchUseCase @Inject constructor(
    private val fileRepository: FileRepository,
) {
    suspend operator fun invoke(query: SearchQuery): Flow<List<FileItem>> =
        fileRepository.search(query)
}
