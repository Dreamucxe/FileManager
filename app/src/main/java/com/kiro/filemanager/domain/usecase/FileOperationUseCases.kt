package com.kiro.filemanager.domain.usecase

import com.kiro.filemanager.domain.model.OperationResult
import com.kiro.filemanager.domain.repository.FileRepository
import com.kiro.filemanager.domain.repository.RecycleBinRepository
import com.kiro.filemanager.domain.util.FileNameValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class CopyUseCase @Inject constructor(
    private val fileRepository: FileRepository,
) {
    operator fun invoke(sources: List<String>, destinationDir: String): Flow<OperationResult> =
        fileRepository.copy(sources, destinationDir)
}

class MoveUseCase @Inject constructor(
    private val fileRepository: FileRepository,
) {
    operator fun invoke(sources: List<String>, destinationDir: String): Flow<OperationResult> =
        fileRepository.move(sources, destinationDir)
}

class RenameUseCase @Inject constructor(
    private val fileRepository: FileRepository,
) {
    suspend operator fun invoke(path: String, newName: String): OperationResult {
        return when (val v = FileNameValidator.validate(newName)) {
            is FileNameValidator.Result.Invalid -> OperationResult.Failure(v.reason)
            FileNameValidator.Result.Valid -> fileRepository.rename(path, newName.trim())
        }
    }
}

class CreateFolderUseCase @Inject constructor(
    private val fileRepository: FileRepository,
) {
    suspend operator fun invoke(parentPath: String, name: String): OperationResult {
        return when (val v = FileNameValidator.validate(name)) {
            is FileNameValidator.Result.Invalid -> OperationResult.Failure(v.reason)
            FileNameValidator.Result.Valid -> fileRepository.createFolder(parentPath, name.trim())
        }
    }
}

class CreateFileUseCase @Inject constructor(
    private val fileRepository: FileRepository,
) {
    suspend operator fun invoke(parentPath: String, name: String): OperationResult {
        return when (val v = FileNameValidator.validate(name)) {
            is FileNameValidator.Result.Invalid -> OperationResult.Failure(v.reason)
            FileNameValidator.Result.Valid -> fileRepository.createFile(parentPath, name.trim())
        }
    }
}

/** Deletes via the recycle bin so the action can be undone. */
class DeleteUseCase @Inject constructor(
    private val recycleBinRepository: RecycleBinRepository,
) {
    operator fun invoke(paths: List<String>): Flow<OperationResult> = flow {
        emit(OperationResult.Progress(0, paths.size, paths.firstOrNull()?.substringAfterLast('/') ?: ""))
        recycleBinRepository.moveToTrash(paths)
            .onSuccess { ids ->
                emit(OperationResult.Success(ids.map { it.toString() }))
            }
            .onFailure { emit(OperationResult.Failure(it.message ?: "Delete failed", it)) }
    }
}
