package com.kiro.filemanager.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import com.kiro.filemanager.core.util.IoDispatcher
import com.kiro.filemanager.domain.model.ApkInfo
import com.kiro.filemanager.domain.model.CertificateSummary
import com.kiro.filemanager.domain.repository.ApkRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApkRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ApkRepository {

    private val pm: PackageManager get() = context.packageManager

    override suspend fun analyzeApk(apkPath: String): Result<ApkInfo> = withContext(io) {
        runCatching {
            val flags = PackageManager.GET_PERMISSIONS or
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SERVICES or
                PackageManager.GET_SIGNING_CERTIFICATES
            val info = pm.getPackageArchiveInfo(apkPath, flags)
                ?: error("Not a valid APK")
            // The appInfo paths must point at the archive to load its label/icon.
            info.applicationInfo?.apply {
                sourceDir = apkPath
                publicSourceDir = apkPath
            }
            val installed = runCatching { pm.getPackageInfo(info.packageName, 0) }.getOrNull()
            info.toApkInfo(File(apkPath), installed)
        }
    }

    override suspend fun getInstalledPackage(packageName: String): Result<ApkInfo> =
        withContext(io) {
            runCatching {
                val flags = PackageManager.GET_PERMISSIONS or
                    PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_SIGNING_CERTIFICATES
                val info = pm.getPackageInfo(packageName, flags)
                info.toApkInfo(File(info.applicationInfo!!.sourceDir), info)
            }
        }

    override suspend fun extractInstalledApk(
        packageName: String,
        destinationDir: String,
    ): Result<String> = withContext(io) {
        runCatching {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val src = File(appInfo.sourceDir)
            val label = pm.getApplicationLabel(appInfo).toString().replace(Regex("[^A-Za-z0-9._-]"), "_")
            val dest = File(destinationDir, "$label-$packageName.apk")
            src.copyTo(dest, overwrite = true)
            dest.absolutePath
        }
    }

    private fun PackageInfo.toApkInfo(apkFile: File, installed: PackageInfo?): ApkInfo {
        val appInfo: ApplicationInfo? = applicationInfo
        val label = appInfo?.let { pm.getApplicationLabel(it).toString() } ?: packageName
        val isSystem = appInfo?.let {
            (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } ?: false
        return ApkInfo(
            label = label,
            packageName = packageName,
            versionName = versionName ?: "—",
            versionCode = longVersionCodeCompat(),
            minSdk = appInfo?.minSdkVersion ?: 0,
            targetSdk = appInfo?.targetSdkVersion ?: 0,
            permissions = requestedPermissions?.toList().orEmpty(),
            activities = activities?.mapNotNull { it.name }.orEmpty(),
            services = services?.mapNotNull { it.name }.orEmpty(),
            isInstalled = installed != null,
            isSystemApp = isSystem,
            installedVersionCode = installed?.longVersionCodeCompat(),
            apkPath = apkFile.absolutePath,
            fileSize = apkFile.length(),
            certificateSummary = extractCertificate(),
        )
    }

    private fun PackageInfo.longVersionCodeCompat(): Long = longVersionCode

    private fun PackageInfo.extractCertificate(): CertificateSummary? {
        val signature: Signature = signingInfo?.let {
            (if (it.hasMultipleSigners()) it.apkContentsSigners else it.signingCertificateHistory)
                ?.firstOrNull()
        } ?: return null

        return runCatching {
            val cf = CertificateFactory.getInstance("X.509")
            val cert = cf.generateCertificate(signature.toByteArray().inputStream()) as X509Certificate
            CertificateSummary(
                subject = cert.subjectX500Principal.name,
                issuer = cert.issuerX500Principal.name,
                serialNumber = cert.serialNumber.toString(16),
                sha1 = signature.digest("SHA-1"),
                sha256 = signature.digest("SHA-256"),
                validFrom = cert.notBefore.time,
                validUntil = cert.notAfter.time,
                signatureAlgorithm = cert.sigAlgName,
            )
        }.getOrNull()
    }

    private fun Signature.digest(algorithm: String): String {
        val md = MessageDigest.getInstance(algorithm)
        return md.digest(toByteArray()).joinToString(":") { "%02X".format(it) }
    }
}
