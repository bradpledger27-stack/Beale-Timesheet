package nz.co.bealetimesheet.ui.endshift

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun EndShiftScreen(
    isSaving: Boolean,
    errorMessage: String?,
    onSave: (
        finishTime: String,
        comments: String
    ) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    var finishTime by rememberSaveable {
        mutableStateOf(
            LocalTime.now()
                .withSecond(0)
                .withNano(0)
                .format(timeFormatter)
        )
    }

    var comments by rememberSaveable {
        mutableStateOf("")
    }

    fun openFinishTimePicker() {
        val currentTime = LocalTime.parse(
            finishTime,
            timeFormatter
        )

        TimePickerDialog(
            context,
            { _, hour, minute ->
                finishTime = LocalTime.of(
                    hour,
                    minute
                ).format(timeFormatter)
            },
            currentTime.hour,
            currentTime.minute,
            true
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "End Shift",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Finish Time",
            style = MaterialTheme.typography.labelLarge
        )

        OutlinedButton(
            onClick = { openFinishTimePicker() },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(finishTime)
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = comments,
            onValueChange = { comments = it },
            label = {
                Text("Comments")
            },
            enabled = !isSaving,
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 18.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onCancel,
                enabled = !isSaving,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    onSave(
                        finishTime,
                        comments
                    )
                },
                enabled = !isSaving,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    if (isSaving) {
                        "Saving..."
                    } else {
                        "Save"
                    }
                )
            }
        }
    }
}