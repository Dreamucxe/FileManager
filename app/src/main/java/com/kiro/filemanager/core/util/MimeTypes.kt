package com.kiro.filemanager.core.util

import android.webkit.MimeTypeMap

/** Resolves MIME types from file extensions with sensible fallbacks. */
object MimeTypes {
    private val overrides = mapOf(
        "kt" to "text/x-kotlin",
        "kts" to "text/x-kotlin",
        "md" to "text/markdown",
        "log" to "text/plain",
        "yml" to "text/yaml",
        "yaml" to "text/yaml",
        "toml" to "text/plain",
        "apk" to "application/vnd.android.package-archive",
        "7z" to "application/x-7z-compressed",
        "rar" to "application/vnd.rar",
        "tar" to "application/x-tar",
        "gz" to "application/gzip",
    )

    fun fromExtension(extension: String): String {
        if (extension.isEmpty()) return "application/octet-stream"
        val ext = extension.lowercase()
        overrides[ext]?.let { return it }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            ?: "application/octet-stream"
    }

    fun fromName(name: String): String =
        fromExtension(name.substringAfterLast('.', ""))
}
