package com.kiro.filemanager.data.datasource

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import com.kiro.filemanager.domain.model.StorageType
import com.kiro.filemanager.domain.model.StorageVolumeInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enumerates mounted storage volumes (internal, SD card, USB OTG) using
 * [StorageManager], falling back to reflection-free [File] stats for capacity.
 */
@Singleton
class StorageVolumeDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val storageManager =
        context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

    fun getVolumes(): List<StorageVolumeInfo> {
        val result = mutableListOf<StorageVolumeInfo>()

        // Primary/internal storage is always present.
        val primary = Environment.getExternalStorageDirectory()
        result += buildInfo(
            volume = null,
            directory = primary,
            fallbackLabel = "Internal Storage",
            type = StorageType.INTERNAL,
            removable = false,
            emulated = true,
        )

        // Removable volumes (SD card, USB OTG) via StorageManager.
        storageManager.storageVolumes.forEach { volume ->
            val dir = volume.directoryCompat() ?: return@forEach
            if (dir.absolutePath == primary.absolutePath) return@forEach
            val type = when {
                volume.isRemovable && isUsb(volume) -> StorageType.USB_OTG
                volume.isRemovable -> StorageType.SD_CARD
                else -> StorageType.SD_CARD
            }
            result += buildInfo(
                volume = volume,
                directory = dir,
                fallbackLabel = if (type == StorageType.USB_OTG) "USB Storage" else "SD Card",
                type = type,
                removable = volume.isRemovable,
                emulated = volume.isEmulated,
            )
        }
        return result
    }

    private fun buildInfo(
        volume: StorageVolume?,
        directory: File,
        fallbackLabel: String,
        type: StorageType,
        removable: Boolean,
        emulated: Boolean,
    ): StorageVolumeInfo {
        val label = volume?.getDescription(context)?.takeIf { it.isNotBlank() } ?: fallbackLabel
        val total = runCatching { directory.totalSpace }.getOrDefault(0L)
        val avail = runCatching { directory.usableSpace }.getOrDefault(0L)
        return StorageVolumeInfo(
            type = type,
            label = label,
            path = directory.absolutePath,
            totalBytes = total,
            availableBytes = avail,
            isRemovable = removable,
            isEmulated = emulated,
        )
    }

    private fun isUsb(volume: StorageVolume): Boolean {
        // Heuristic: USB volumes typically expose a description mentioning USB.
        val desc = runCatching { volume.getDescription(context) }.getOrNull()?.lowercase().orEmpty()
        return "usb" in desc
    }

    private fun StorageVolume.directoryCompat(): File? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            directory?.let { return it }
        }
        // Older devices / null directory: derive from the UUID mount point under /storage.
        return runCatching {
            uuid?.let { File("/storage/$it") }
        }.getOrNull()?.takeIf { it.exists() }
    }
}
