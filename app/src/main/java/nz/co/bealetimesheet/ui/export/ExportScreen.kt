package nz.co.bealetimesheet.ui.export

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ExportScreen(
    recipientEmail: String,
    onRecipientEmailChange: (String) -> Unit,
    hasPreviewed: Boolean,
    onPreviewPdf: () -> Unit,
    onSignAndSubmit: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Review and Email Timesheet",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = if (hasPreviewed) {
                "Check the preview, then sign and submit."
            } else {
                "Preview the completed timesheet before signing."
            },
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = recipientEmail,
            onValueChange = onRecipientEmailChange,
            label = { Text("Recipient email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (!hasPreviewed) {
            Button(
                onClick = onPreviewPdf,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Preview Completed Timesheet")
            }
        } else {
            OutlinedButton(
                onClick = onPreviewPdf,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Preview Again")
            }

            Button(
                onClick = onSignAndSubmit,
                enabled = recipientEmail.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign and Submit")
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}