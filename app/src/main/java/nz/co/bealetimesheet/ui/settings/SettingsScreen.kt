package nz.co.bealetimesheet.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    initialEmployeeName: String,
    initialRecipientEmail: String,
    initialTuesdayReminderEnabled: Boolean,
    initialActiveShiftReminderEnabled: Boolean,
    onSave: (
        employeeName: String,
        recipientEmail: String,
        tuesdayReminderEnabled: Boolean,
        activeShiftReminderEnabled: Boolean
    ) -> Unit,
    onCancel: () -> Unit
) {
    var employeeName by remember(initialEmployeeName) {
        mutableStateOf(initialEmployeeName)
    }

    var recipientEmail by remember(initialRecipientEmail) {
        mutableStateOf(initialRecipientEmail)
    }

    var tuesdayReminderEnabled by remember(
        initialTuesdayReminderEnabled
    ) {
        mutableStateOf(initialTuesdayReminderEnabled)
    }

    var activeShiftReminderEnabled by remember(
        initialActiveShiftReminderEnabled
    ) {
        mutableStateOf(initialActiveShiftReminderEnabled)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings")
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = employeeName,
                onValueChange = {
                    employeeName = it
                    errorMessage = null
                },
                label = {
                    Text("Employee name")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Reminders",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tuesday submission reminder")
                    Text(
                        text = "Remind me to review and submit the pay week.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = tuesdayReminderEnabled,
                    onCheckedChange = {
                        tuesdayReminderEnabled = it
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Active-shift reminder")
                    Text(
                        text = "Show a reminder when the app opens " +
                            "with a shift still active.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = activeShiftReminderEnabled,
                    onCheckedChange = {
                        activeShiftReminderEnabled = it
                    }
                )
            }

            OutlinedTextField(
                value = recipientEmail,
                onValueChange = {
                    recipientEmail = it
                    errorMessage = null
                },
                label = {
                    Text("Default export email")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Text(it)
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Button(
                onClick = {
                    when {
                        employeeName.isBlank() -> {
                            errorMessage =
                                "Employee name cannot be empty."
                        }

                        recipientEmail.isBlank() -> {
                            errorMessage =
                                "Email address cannot be empty."
                        }

                        else -> {
                            onSave(
                                employeeName.trim(),
                                recipientEmail.trim(),
                                tuesdayReminderEnabled,
                                activeShiftReminderEnabled
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}
