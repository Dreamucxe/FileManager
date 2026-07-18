package com.kiro.filemanager.domain.model

/**
 * A file moved to the app-managed recycle bin. [trashPath] is the physical
 * location inside the app's trash directory; [originalPath] is where it will
 * be restored to.
 */
data class TrashItem(
    val id: Long,
    val originalPath: String,
    val trashPath: String,
    val name: String,
    val size: Long,
    val isDirectory: Boolean,
    val deletedAt: Long,
)
