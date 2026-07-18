package com.kiro.filemanager.domain.model

enum class StorageType { INTERNAL, SD_CARD, USB_OTG }

/**
 * A mounted storage volume with capacity information for the dashboard.
 */
data class StorageVolumeInfo(
    val type: StorageType,
    val label: String,
    val path: String,
    val totalBytes: Long,
    val availableBytes: Long,
    val isRemovable: Boolean,
    val isEmulated: Boolean,
) {
    val usedBytes: Long get() = (totalBytes - availableBytes).coerceAtLeast(0)
    val usedFraction: Float
        get() = if (totalBytes <= 0) 0f else (usedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
}
