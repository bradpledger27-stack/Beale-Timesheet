package nz.co.bealetimesheet.ui.restbreak

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
fun RestBreakScreen(
    isSaving: Boolean,
    errorMessage: String?,
    onSave: (
        breakStartTime: String,
        breakFinishTime: String
    ) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val defaultStartTime = LocalTime.now()
        .withSecond(0)
        .withNano(0)

    val defaultFinishTime = defaultStartTime.plusMinutes(30)

    var breakStartTime by rememberSaveable {
        mutableStateOf(defaultStartTime.format(timeFormatter))
    }

    var breakFinishTime by rememberSaveable {
        mutableStateOf(defaultFinishTime.format(timeFormatter))
    }

    fun openStartTimePicker() {
        val currentTime = LocalTime.parse(
            breakStartTime,
            timeFormatter
        )

        TimePickerDialog(
            context,
            { _, hour, minute ->
                val newStartTime = LocalTime.of(hour, minute)

                breakStartTime = newStartTime.format(timeFormatter)

                /*
                * Whenever the start time is changed,
                * the finish time defaults to 30 minutes later.
                * The finish time can still be changed afterwards.
                */
                breakFinishTime = newStartTime
                    .plusMinutes(30)
                    .format(timeFormatter)
            },
            currentTime.hour,
            currentTime.minute,
            true
        ).show()
    }

    fun openFinishTimePicker() {
        val currentTime = LocalTime.parse(
            breakFinishTime,
            timeFormatter
        )

        TimePickerDialog(
            context,
            { _, hour, minute ->
                breakFinishTime = LocalTime.of(
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
            text = "Rest Break",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Break Start",
            style = MaterialTheme.typography.labelLarge
        )

        OutlinedButton(
            onClick = { openStartTimePicker() },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(breakStartTime)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Break Finish",
            style = MaterialTheme.typography.labelLarge
        )

        OutlinedButton(
            onClick = { openFinishTimePicker() },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(breakFinishTime)
        }

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
                        breakStartTime,
                        breakFinishTime
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