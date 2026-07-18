package com.kiro.filemanager.domain.util

import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/** Pure formatting helpers, unit-testable without Android. */
object Formatter {

    /** Formats a byte count using binary units (1024) — e.g. 1.5 GiB shown as "1.50 GB". */
    fun formatSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
        var value = bytes.toDouble()
        var b = bytes
        while (abs(b) >= 1024 && ci.index < ci.endIndex) {
            value = b / 1024.0
            b /= 1024
            ci.next()
        }
        // Correct the value using the true byte count for precision.
        val exp = ci.index + 1
        val divisor = Math.pow(1024.0, exp.toDouble())
        val v = bytes / divisor
        return String.format(Locale.US, "%.2f %sB", v, ci.current())
    }

    private val dateFormat = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault())
    private val dateFormatShort = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    fun formatDate(epochMillis: Long): String =
        if (epochMillis <= 0) "—" else dateFormat.format(Date(epochMillis))

    fun formatDateShort(epochMillis: Long): String =
        if (epochMillis <= 0) "—" else dateFormatShort.format(Date(epochMillis))

    /** "3 items", "1 item", "Empty". */
    fun formatItemCount(count: Int): String = when {
        count < 0 -> ""
        count == 0 -> "Empty"
        count == 1 -> "1 item"
        else -> "$count items"
    }
}
