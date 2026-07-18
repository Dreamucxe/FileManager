package com.kiro.filemanager.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kiro.filemanager.domain.model.FileItem
import com.kiro.filemanager.domain.model.FileType
import com.kiro.filemanager.feature.apk.ApkInfoScreen
import com.kiro.filemanager.feature.archive.ArchiveScreen
import com.kiro.filemanager.feature.textviewer.TextViewerScreen
import com.kiro.filemanager.presentation.browse.BrowseScreen
import com.kiro.filemanager.presentation.home.HomeScreen
import com.kiro.filemanager.presentation.recyclebin.RecycleBinScreen
import com.kiro.filemanager.presentation.search.SearchScreen
import com.kiro.filemanager.presentation.settings.SettingsScreen

private data class TabItem(
    /** Route pattern used to detect the selected state. */
    val matchRoute: String,
    /** Concrete route to navigate to when tapped. */
    val target: String,
    val label: String,
    val icon: ImageVector,
)

private val tabs = listOf(
    TabItem(Routes.HOME, Routes.HOME, "Home", Icons.Filled.Home),
    TabItem(Routes.BROWSE_ROUTE, Routes.BROWSE, "Browse", Icons.Filled.Folder),
    TabItem(Routes.SEARCH_ROUTE, Routes.SEARCH, "Search", Icons.Filled.Search),
    TabItem(Routes.SETTINGS, Routes.SETTINGS, "Settings", Icons.Filled.Settings),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KiroNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val showBottomBar = currentRoute in tabs.map { it.matchRoute }

    // Route a non-directory file to the correct viewer.
    fun openFile(item: FileItem) {
        when (item.type) {
            FileType.APK -> navController.navigate(Routes.apkInfo(item.path))
            FileType.ARCHIVE -> navController.navigate(Routes.archiveViewer(item.path))
            else -> if (item.type.isTextEditable) navController.navigate(Routes.textViewer(item.path))
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.matchRoute,
                            onClick = {
                                navController.navigate(tab.target) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenPath = { path -> navController.navigate(Routes.browse(path)) },
                    onOpenCategory = { category ->
                        navController.navigate(Routes.search(category.toSearchFilter().name))
                    },
                )
            }

            composable(
                route = Routes.BROWSE_ROUTE,
                arguments = listOf(
                    navArgument(Routes.BROWSE_ARG_PATH) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) {
                BrowseScreen(
                    onNavigateToFolder = { path -> navController.navigate(Routes.browse(path)) },
                    onNavigateUp = { if (!navController.popBackStack()) Unit },
                    onOpenTextFile = { path -> navController.navigate(Routes.textViewer(path)) },
                    onOpenApk = { path -> navController.navigate(Routes.apkInfo(path)) },
                    onOpenArchive = { path -> navController.navigate(Routes.archiveViewer(path)) },
                )
            }

            composable(
                route = Routes.SEARCH_ROUTE,
                arguments = listOf(
                    navArgument(Routes.SEARCH_ARG_FILTER) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) {
                SearchScreen(
                    onOpenFolder = { path -> navController.navigate(Routes.browse(path)) },
                    onOpenFile = ::openFile,
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen()
            }

            composable(Routes.RECYCLE_BIN) {
                RecycleBinScreen(onNavigateUp = { navController.popBackStack() })
            }

            composable(
                route = Routes.TEXT_VIEWER_ROUTE,
                arguments = listOf(navArgument(Routes.TEXT_VIEWER_ARG_PATH) { type = NavType.StringType }),
            ) {
                TextViewerScreen(onNavigateUp = { navController.popBackStack() })
            }

            composable(
                route = Routes.APK_INFO_ROUTE,
                arguments = listOf(navArgument(Routes.APK_INFO_ARG_PATH) { type = NavType.StringType }),
            ) {
                ApkInfoScreen(onNavigateUp = { navController.popBackStack() })
            }

            composable(
                route = Routes.ARCHIVE_VIEWER_ROUTE,
                arguments = listOf(navArgument(Routes.ARCHIVE_VIEWER_ARG_PATH) { type = NavType.StringType }),
            ) {
                ArchiveScreen(onNavigateUp = { navController.popBackStack() })
            }
        }
    }
}
