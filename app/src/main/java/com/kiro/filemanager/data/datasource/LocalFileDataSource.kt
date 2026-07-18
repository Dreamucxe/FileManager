package com.kiro.filemanager.data.datasource

import com.kiro.filemanager.core.util.MimeTypes
import com.kiro.filemanager.domain.model.FileItem
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Direct java.io-backed file access for paths the app can read/write without SAF.
 * With MANAGE_EXTERNAL_STORAGE granted this covers essentially all of shared
 * storage on the device.
 */
@Singleton
class LocalFileDataSource @Inject constructor() {

    fun toFileItem(file: File): FileItem {
        val name = file.name
        val ext = if (file.isDirectory) "" else name.substringAfterLast('.', "")
        return FileItem(
            path = file.absolutePath,
            name = name,
            isDirectory = file.isDirectory,
            size = if (file.isDirectory) 0 else file.length(),
            lastModified = file.lastModified(),
            extension = ext,
            mimeType = if (file.isDirectory) "resource/folder" else MimeTypes.fromExtension(ext),
            isHidden = name.startsWith('.'),
            canRead = file.canRead(),
            canWrite = file.canWrite(),
            childCount = -1,
        )
    }

    fun list(path: String, showHidden: Boolean): List<FileItem> {
        val dir = File(path)
        if (!dir.isDirectory) return emptyList()
        val children = dir.listFiles() ?: return emptyList()
        return children
            .asSequence()
            .filter { showHidden || !it.name.startsWith('.') }
            .map { toFileItem(it) }
            .toList()
    }

    fun getItem(path: String): FileItem? {
        val file = File(path)
        return if (file.exists()) toFileItem(file) else null
    }

    fun exists(path: String): Boolean = File(path).exists()

    /** Recursively sums the size of all regular files below [path]. */
    fun computeSize(path: String): Long {
        val root = File(path)
        if (root.isFile) return root.length()
        var total = 0L
        root.walkTopDown().forEach { if (it.isFile) total += it.length() }
        return total
    }

    fun countChildren(path: String): Int {
        val dir = File(path)
        if (!dir.isDirectory) return -1
        return dir.list()?.size ?: 0
    }

    fun mkdirs(parentPath: String, name: String): File {
        val target = File(parentPath, name)
        target.mkdirs()
        return target
    }

    fun createNewFile(parentPath: String, name: String): File {
        val target = File(parentPath, name)
        target.parentFile?.mkdirs()
        target.createNewFile()
        return target
    }

    fun rename(path: String, newName: String): Boolean {
        val src = File(path)
        val dest = File(src.parentFile, newName)
        return src.renameTo(dest)
    }

    fun delete(path: String): Boolean {
        val file = File(path)
        return if (file.isDirectory) file.deleteRecursively() else file.delete()
    }
}
