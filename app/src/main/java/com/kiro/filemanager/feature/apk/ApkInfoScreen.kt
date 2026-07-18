package com.kiro.filemanager.feature.apk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kiro.filemanager.domain.model.ApkInfo
import com.kiro.filemanager.presentation.util.formatBytes
import com.kiro.filemanager.presentation.util.openFileExternally

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkInfoScreen(
    onNavigateUp: () -> Unit,
    viewModel: ApkInfoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("APK Info") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            if (state.info != null) {
                ExtendedFloatingActionButton(
                    text = { Text("Install") },
                    icon = { Icon(Icons.Filled.InstallMobile, contentDescription = null) },
                    onClick = { context.openFileExternally(viewModel.apkPath) },
                )
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> Text(
                    state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center),
                )
                state.info != null -> ApkDetails(state.info!!)
            }
        }
    }
}

@Composable
private fun ApkDetails(info: ApkInfo) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(info.label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(info.packageName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Divider(Modifier.padding(vertical = 12.dp))

        InfoRow("Version", "${info.versionName} (${info.versionCode})")
        InfoRow("Min SDK", info.minSdk.toString())
        InfoRow("Target SDK", info.targetSdk.toString())
        InfoRow("Size", formatBytes(info.fileSize))
        InfoRow("Installed", if (info.isInstalled) "Yes (v${info.installedVersionCode})" else "No")
        InfoRow("Permissions", info.permissions.size.toString())
        InfoRow("Activities", info.activities.size.toString())
        InfoRow("Services", info.services.size.toString())

        info.certificateSummary?.let { cert ->
            Divider(Modifier.padding(vertical = 12.dp))
            Text("Signing certificate", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            InfoRow("Subject", cert.subject)
            InfoRow("Issuer", cert.issuer)
            InfoRow("Algorithm", cert.signatureAlgorithm)
            InfoRow("SHA-256", cert.sha256)
        }

        if (info.permissions.isNotEmpty()) {
            Divider(Modifier.padding(vertical = 12.dp))
            Text("Permissions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            info.permissions.forEach { perm ->
                Text(
                    perm,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 1.dp),
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.6f))
    }
}
