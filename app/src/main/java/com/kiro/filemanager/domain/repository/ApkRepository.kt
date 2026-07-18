package com.kiro.filemanager.domain.repository

import com.kiro.filemanager.domain.model.ApkInfo
import com.kiro.filemanager.domain.model.ExifData

/** APK analysis + installed-package inspection. */
interface ApkRepository {
    suspend fun analyzeApk(apkPath: String): Result<ApkInfo>
    suspend fun getInstalledPackage(packageName: String): Result<ApkInfo>
    /** Extracts an installed app's base APK to [destinationDir]. */
    suspend fun extractInstalledApk(packageName: String, destinationDir: String): Result<String>
}

/** Image metadata extraction, kept separate so it can back the image viewer. */
interface MetadataRepository {
    suspend fun readExif(imagePath: String): Result<ExifData>
}
