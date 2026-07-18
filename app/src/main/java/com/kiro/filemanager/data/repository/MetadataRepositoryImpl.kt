package com.kiro.filemanager.data.repository

import androidx.exifinterface.media.ExifInterface
import com.kiro.filemanager.core.util.IoDispatcher
import com.kiro.filemanager.domain.model.ExifData
import com.kiro.filemanager.domain.repository.MetadataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataRepositoryImpl @Inject constructor(
    @IoDispatcher private val io: CoroutineDispatcher,
) : MetadataRepository {

    override suspend fun readExif(imagePath: String): Result<ExifData> = withContext(io) {
        runCatching {
            val exif = ExifInterface(imagePath)
            val latLong = FloatArray(2)
            val hasLocation = exif.getLatLong(latLong)
            ExifData(
                width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0).takeIf { it > 0 },
                height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0).takeIf { it > 0 },
                make = exif.getAttribute(ExifInterface.TAG_MAKE),
                model = exif.getAttribute(ExifInterface.TAG_MODEL),
                dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME),
                exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME),
                aperture = exif.getAttribute(ExifInterface.TAG_F_NUMBER),
                iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY),
                focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH),
                flash = exif.getAttribute(ExifInterface.TAG_FLASH),
                latitude = if (hasLocation) latLong[0].toDouble() else null,
                longitude = if (hasLocation) latLong[1].toDouble() else null,
                orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                ),
            )
        }
    }
}
