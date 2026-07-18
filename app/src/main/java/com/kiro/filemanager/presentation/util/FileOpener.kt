package com.kiro.filemanager.presentation.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.kiro.filemanager.core.util.MimeTypes
import java.io.File

/** Opens a file with an external app via a shared FileProvider URI. */
fun Context.openFileExternally(path: String) {
    val file = File(path)
    if (!file.exists()) {
        Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
        return
    }
    val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    val mime = MimeTypes.fromExtension(file.extension)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mime)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching {
        startActivity(Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.onFailure {
        Toast.makeText(this, "No app can open this file", Toast.LENGTH_SHORT).show()
    }
}

/** Shares one or more files through the system share sheet. */
fun Context.shareFiles(paths: List<String>) {
    val uris = ArrayList<android.net.Uri>()
    paths.forEach { p ->
        val f = File(p)
        if (f.exists() && f.isFile) {
            uris.add(FileProvider.getUriForFile(this, "$packageName.fileprovider", f))
        }
    }
    if (uris.isEmpty()) return
    val intent = if (uris.size == 1) {
        Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uris.first())
        }
    } else {
        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        }
    }
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
