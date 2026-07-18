package com.kiro.filemanager.domain.model

/** EXIF attributes extracted from an image, shown in the image details sheet. */
data class ExifData(
    val width: Int?,
    val height: Int?,
    val make: String?,
    val model: String?,
    val dateTime: String?,
    val exposureTime: String?,
    val aperture: String?,
    val iso: String?,
    val focalLength: String?,
    val flash: String?,
    val latitude: Double?,
    val longitude: Double?,
    val orientation: Int?,
)

/** A single entry in the archive listing preview. */
data class ArchiveEntry(
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val compressedSize: Long,
    val lastModified: Long,
)
