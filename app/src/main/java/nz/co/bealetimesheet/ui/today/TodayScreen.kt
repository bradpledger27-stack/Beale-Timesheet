package nz.co.bealetimesheet.ui.today

import android.app.Application
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen() {

    val context = LocalContext.current
    val application = context.applicationContext as Application

    val factory = remember(application) {
        TodayViewModelFactory(application)
    }

    val todayViewModel: TodayViewModel = viewModel(
        factory = factory
    )

    val uiState by todayViewModel.uiState.collectAsState()

    val displayDate = remember(uiState.date) {
        try {
            LocalDate.parse(uiState.date).format(
                DateTimeFormatter.ofPattern(
                    "EEEE dd MMMM yyyy",
                    Locale.getDefault()
                )
            )
        } catch (_: Exception) {
            uiState.date
        }
    }

    if (uiState.isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()

            Text(
                text = "Loading today's entry...",
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Today's Entry",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = uiState.employeeName,
            onValueChange = todayViewModel::updateEmployeeName,
            label = { Text("Employee Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Date: $`displayDate",
            style = MaterialTheme.typography.bodyLarge
        )

        TimeSelectionRow(
            label = "Start Time",
            time = uiState.startTime,
            onTimeSelected = todayViewModel::updateStartTime
        )

        TimeSelectionRow(
            label = "Finish Time",
            time = uiState.finishTime,
            onTimeSelected = todayViewModel::updateFinishTime
        )

        Text(
            text = "Break",
            style = MaterialTheme.typography.titleMedium
        )

        if (
            uiState.breakStartTime == null ||
            uiState.breakFinishTime == null
        ) {
            Button(
                onClick = {
                    showTimePicker(
                        context = context,
                        currentValue = null,
                        useThirtyMinuteIncrements = true
                    ) { selectedStart ->

                        val formatter =
                            DateTimeFormatter.ofPattern("HH:mm")

                        val start = LocalTime.parse(
                            selectedStart,
                            formatter
                        )

                        val finish = start.plusMinutes(30)

                        todayViewModel.updateBreakTimes(
                            startTime = start.format(formatter),
                            finishTime = finish.format(formatter)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add 30-Minute Break")
            }
        } else {
            Text(
                text = "`${uiState.breakStartTime} – " +
                        "${uiState.breakFinishTime}",
                style = MaterialTheme.typography.bodyLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        showTimePicker(
                            context = context,
                            currentValue = uiState.breakStartTime,
                            useThirtyMinuteIncrements = true
                        ) { selectedStart ->

                            val formatter =
                                DateTimeFormatter.ofPattern("HH:mm")

                            val start = LocalTime.parse(
                                selectedStart,
                                formatter
                            )

                            val finish = start.plusMinutes(30)

                            todayViewModel.updateBreakTimes(
                                startTime = start.format(formatter),
                                finishTime = finish.format(formatter)
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Change Break")
                }

                OutlinedButton(
                    onClick = todayViewModel::removeBreak,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Remove Break")
                }
            }
        }

        OutlinedTextField(
            value = uiState.comments,
            onValueChange = todayViewModel::updateComments,
            label = { Text("Comments") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (uiState.isSaved) {
            Text(
                text = "Entry saved",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = todayViewModel::saveEntry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (uiState.isSaved) {
                    "Saved"
                } else {
                    "Save Entry"
                }
            )
        }
    }
}

@Composable
private fun TimeSelectionRow(
    label: String,
    time: String,
    onTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = time,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("Not selected") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = {
                showTimePicker(
                    context = context,
                    currentValue = time,
                    useThirtyMinuteIncrements = false,
                    onTimeSelected = onTimeSelected
                )
            }
        ) {
            Text("Choose")
        }
    }
}

private fun showTimePicker(
    context: Context,
    currentValue: String?,
    useThirtyMinuteIncrements: Boolean,
    onTimeSelected: (String) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    val initialTime = try {
        if (currentValue.isNullOrBlank()) {
            LocalTime.now()
        } else {
            LocalTime.parse(currentValue, formatter)
        }
    } catch (_: Exception) {
        LocalTime.now()
    }

    TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->

            val adjustedMinute = if (useThirtyMinuteIncrements) {
                if (selectedMinute < 30) {
                    0
                } else {
                    30
                }
            } else {
                selectedMinute
            }

            val selectedTime = LocalTime.of(
                selectedHour,
                adjustedMinute
            )

            onTimeSelected(
                selectedTime.format(formatter)
            )
        },
        initialTime.hour,
        initialTime.minute,
        true
    ).show()
}