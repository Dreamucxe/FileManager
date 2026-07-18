package com.kiro.filemanager.domain.model

/**
 * High-level classification of a file, derived from its extension / MIME type.
 * Drives icons, category filtering, and which viewer/player to open.
 */
enum class FileType {
    FOLDER,
    IMAGE,
    VIDEO,
    AUDIO,
    PDF,
    DOCUMENT,      // doc, docx, odt, rtf, xls, ppt…
    TEXT,          // txt, log, csv
    MARKDOWN,
    JSON,
    XML,
    HTML,
    CODE,          // kt, java, py, js, c, cpp…
    APK,
    ARCHIVE,       // zip, rar, 7z, tar, gz…
    BINARY,
    UNKNOWN;

    val isMedia: Boolean get() = this == IMAGE || this == VIDEO || this == AUDIO

    val isTextEditable: Boolean
        get() = this == TEXT || this == MARKDOWN || this == JSON ||
            this == XML || this == HTML || this == CODE

    companion object {
        private val imageExt = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", "svg", "ico", "tiff")
        private val videoExt = setOf("mp4", "mkv", "webm", "avi", "mov", "flv", "3gp", "m4v", "ts", "mpg", "mpeg", "wmv")
        private val audioExt = setOf("mp3", "wav", "flac", "aac", "ogg", "m4a", "opus", "wma", "amr", "mid", "midi")
        private val docExt = setOf("doc", "docx", "odt", "rtf", "xls", "xlsx", "ods", "ppt", "pptx", "odp", "epub")
        private val textExt = setOf("txt", "log", "csv", "tsv", "ini", "conf", "cfg", "properties", "env")
        private val codeExt = setOf(
            "kt", "kts", "java", "py", "js", "ts", "jsx", "tsx", "c", "cpp", "h", "hpp", "cs",
            "go", "rs", "rb", "php", "swift", "sh", "bash", "yaml", "yml", "toml", "gradle", "sql", "dart"
        )
        private val archiveExt = setOf("zip", "rar", "7z", "tar", "gz", "tgz", "bz2", "xz", "jar", "apks", "xapk")

        fun fromExtension(extension: String): FileType {
            val ext = extension.lowercase()
            return when {
                ext in imageExt -> IMAGE
                ext in videoExt -> VIDEO
                ext in audioExt -> AUDIO
                ext == "pdf" -> PDF
                ext in docExt -> DOCUMENT
                ext == "md" || ext == "markdown" -> MARKDOWN
                ext == "json" -> JSON
                ext == "xml" -> XML
                ext == "html" || ext == "htm" -> HTML
                ext in textExt -> TEXT
                ext in codeExt -> CODE
                ext == "apk" -> APK
                ext in archiveExt -> ARCHIVE
                ext == "bin" || ext == "dat" || ext == "so" || ext == "o" -> BINARY
                else -> UNKNOWN
            }
        }
    }
}
