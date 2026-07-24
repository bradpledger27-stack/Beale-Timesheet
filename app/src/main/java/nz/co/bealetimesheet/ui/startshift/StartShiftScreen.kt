package nz.co.bealetimesheet.ui.startshift

import android.app.DatePickerDialog
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun StartShiftScreen(
    isSaving: Boolean,
    errorMessage: String?,
    onSave: (
        weekStarting: String,
        date: String,
        startTime: String
    ) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    val today = LocalDate.now()
    val currentTime = LocalTime.now()

    var selectedDate by rememberSaveable {
        mutableStateOf(today.toString())
    }

    var selectedWeekStarting by rememberSaveable {
        mutableStateOf(calculateWeekStarting(today).toString())
    }

    var selectedStartTime by rememberSaveable {
        mutableStateOf(
            currentTime.format(
                DateTimeFormatter.ofPattern("HH:mm")
            )
        )
    }

    val displayDateFormatter = DateTimeFormatter.ofPattern(
        "EEE dd MMM yyyy",
        Locale.ENGLISH
    )

    fun openWeekStartingPicker() {
        val currentValue = LocalDate.parse(selectedWeekStarting)

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedWeekStarting = LocalDate.of(
                    year,
                    month + 1,
                    dayOfMonth
                ).toString()
            },
            currentValue.year,
            currentValue.monthValue - 1,
            currentValue.dayOfMonth
        ).show()
    }

    fun openDatePicker() {
        val currentValue = LocalDate.parse(selectedDate)

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newDate = LocalDate.of(
                    year,
                    month + 1,
                    dayOfMonth
                )

                selectedDate = newDate.toString()

                /*
                * Changing the date automatically selects the Wednesday
                * belonging to that pay week.
                */
                selectedWeekStarting =
                    calculateWeekStarting(newDate).toString()
            },
            currentValue.year,
            currentValue.monthValue - 1,
            currentValue.dayOfMonth
        ).show()
    }

    fun openTimePicker() {
        val currentValue = LocalTime.parse(selectedStartTime)

        TimePickerDialog(
            context,
            { _, hour, minute ->
                selectedStartTime = LocalTime.of(
                    hour,
                    minute
                ).format(
                    DateTimeFormatter.ofPattern("HH:mm")
                )
            },
            currentValue.hour,
            currentValue.minute,
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
            text = "Start Shift",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Week Starting",
            style = MaterialTheme.typography.labelLarge
        )

        OutlinedButton(
            onClick = { openWeekStartingPicker() },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(
                LocalDate.parse(selectedWeekStarting)
                    .format(displayDateFormatter)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Date",
            style = MaterialTheme.typography.labelLarge
        )

        OutlinedButton(
            onClick = { openDatePicker() },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(
                LocalDate.parse(selectedDate)
                    .format(displayDateFormatter)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Start Time",
            style = MaterialTheme.typography.labelLarge
        )

        OutlinedButton(
            onClick = { openTimePicker() },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(selectedStartTime)
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
                        selectedWeekStarting,
                        selectedDate,
                        selectedStartTime
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

private fun calculateWeekStarting(
    date: LocalDate
): LocalDate {
    return date.with(
        TemporalAdjusters.previousOrSame(
            DayOfWeek.WEDNESDAY
        )
    )
}