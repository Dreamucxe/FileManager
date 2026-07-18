package com.kiro.filemanager.domain.model

/**
 * Result of a file operation. [Progress] is emitted for long-running batch ops
 * (copy/move/compress) so the UI can show determinate progress.
 */
sealed interface OperationResult {
    data class Progress(
        val processed: Int,
        val total: Int,
        val currentName: String,
        val bytesProcessed: Long = 0,
        val bytesTotal: Long = 0,
    ) : OperationResult {
        val fraction: Float get() = if (total <= 0) 0f else processed.toFloat() / total
    }

    data class Success(val affected: List<String>) : OperationResult

    data class Failure(val message: String, val cause: Throwable? = null) : OperationResult
}

/**
 * Describes a completed operation so it can be reversed by the undo manager.
 */
sealed interface UndoableAction {
    data class Move(val from: List<String>, val to: List<String>) : UndoableAction
    data class Delete(val trashedIds: List<Long>) : UndoableAction
    data class Rename(val path: String, val oldName: String, val newName: String) : UndoableAction
    data class Create(val path: String) : UndoableAction
}
