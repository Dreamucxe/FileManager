package com.kiro.filemanager.presentation.util

import android.text.format.DateUtils
import java.util.Locale

/** Human-readable byte counts (e.g. 1.4 GB) using binary units. */
fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB", "PB")
    var value = bytes.toDouble()
    var unit = -1
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    return String.format(Locale.US, "%.1f %s", value, units[unit])
}

/** Relative time for listings ("3 hours ago"), falling back to absolute for old files. */
fun formatRelativeTime(epochMillis: Long): String {
    if (epochMillis <= 0) return "—"
    return DateUtils.getRelativeTimeSpanString(
        epochMillis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()
}
