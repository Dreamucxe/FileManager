package com.kiro.filemanager.presentation.navigation

import android.net.Uri

/** Type-safe route definitions for the app's navigation graph. */
object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val RECYCLE_BIN = "recycle_bin"
    const val FAVORITES = "favorites"

    const val SEARCH = "search"
    const val SEARCH_ARG_FILTER = "filter"
    const val SEARCH_ROUTE = "$SEARCH?$SEARCH_ARG_FILTER={$SEARCH_ARG_FILTER}"

    /** Opens Search preloaded with [filter] (a [SearchFilter] name). */
    fun search(filter: String): String = "$SEARCH?$SEARCH_ARG_FILTER=$filter"

    const val BROWSE = "browse"
    const val BROWSE_ARG_PATH = "path"
    const val BROWSE_ROUTE = "$BROWSE?$BROWSE_ARG_PATH={$BROWSE_ARG_PATH}"

    fun browse(path: String): String =
        "$BROWSE?$BROWSE_ARG_PATH=${Uri.encode(path)}"

    const val TEXT_VIEWER = "text_viewer"
    const val TEXT_VIEWER_ARG_PATH = "path"
    const val TEXT_VIEWER_ROUTE = "$TEXT_VIEWER/{$TEXT_VIEWER_ARG_PATH}"
    fun textViewer(path: String): String = "$TEXT_VIEWER/${Uri.encode(path)}"

    const val APK_INFO = "apk_info"
    const val APK_INFO_ARG_PATH = "path"
    const val APK_INFO_ROUTE = "$APK_INFO/{$APK_INFO_ARG_PATH}"
    fun apkInfo(path: String): String = "$APK_INFO/${Uri.encode(path)}"

    const val ARCHIVE_VIEWER = "archive_viewer"
    const val ARCHIVE_VIEWER_ARG_PATH = "path"
    const val ARCHIVE_VIEWER_ROUTE = "$ARCHIVE_VIEWER/{$ARCHIVE_VIEWER_ARG_PATH}"
    fun archiveViewer(path: String): String = "$ARCHIVE_VIEWER/${Uri.encode(path)}"
}
