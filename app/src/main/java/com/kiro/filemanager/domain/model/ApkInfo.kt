package com.kiro.filemanager.domain.model

/**
 * Metadata extracted from an APK file or an installed package, used by the
 * APK analyzer / installer / package info screens.
 */
data class ApkInfo(
    val label: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val minSdk: Int,
    val targetSdk: Int,
    val permissions: List<String>,
    val activities: List<String>,
    val services: List<String>,
    val isInstalled: Boolean,
    val isSystemApp: Boolean,
    val installedVersionCode: Long?,
    val apkPath: String,
    val fileSize: Long,
    val certificateSummary: CertificateSummary?,
)

/**
 * Signing certificate details for the certificate viewer.
 */
data class CertificateSummary(
    val subject: String,
    val issuer: String,
    val serialNumber: String,
    val sha1: String,
    val sha256: String,
    val validFrom: Long,
    val validUntil: Long,
    val signatureAlgorithm: String,
)
