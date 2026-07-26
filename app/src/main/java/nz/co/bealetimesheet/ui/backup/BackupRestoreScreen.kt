package nz.co.bealetimesheet.ui.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun BackupRestoreScreen(
    onExport: suspend (Uri) -> Unit,
    onRestore: suspend (Uri) -> Unit,
    onRestored: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var isWorking by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isWorking = true
                message = runCatching {
                    onExport(uri)
                    "Backup saved successfully."
                }.getOrElse {
                    "Backup failed: ${it.message ?: "Unknown error"}"
                }
                isWorking = false
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Backup & Restore",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Save a complete copy of your timesheets, settings, " +
                "submission status and signature."
        )

        Button(
            onClick = {
                exportLauncher.launch(
                    "beale-timesheet-backup-${LocalDate.now()}.json"
                )
            },
            enabled = !isWorking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Backup")
        }

        OutlinedButton(
            onClick = {
                restoreLauncher.launch(arrayOf("application/json", "text/*"))
            },
            enabled = !isWorking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Restore from Backup")
        }

        message?.let {
            Text(
                text = it,
                color = if (it.startsWith("Backup failed")) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }

        OutlinedButton(
            onClick = onBack,
            enabled = !isWorking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }

    pendingRestoreUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            title = { Text("Replace current app data?") },
            text = {
                Text(
                    "Restoring this backup will replace all timesheets, " +
                        "settings and the saved signature currently in " +
                        "the app."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingRestoreUri = null
                        scope.launch {
                            isWorking = true
                            val result = runCatching { onRestore(uri) }
                            isWorking = false
                            if (result.isSuccess) {
                                onRestored()
                            } else {
                                message = "Restore failed: " +
                                    (result.exceptionOrNull()?.message
                                        ?: "Unknown error")
                            }
                        }
                    }
                ) {
                    Text("Replace and Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreUri = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
