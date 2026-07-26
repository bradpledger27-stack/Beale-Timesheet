package nz.co.bealetimesheet.ui.currenttimesheet

import android.app.TimePickerDialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nz.co.bealetimesheet.data.model.RestBreak
import nz.co.bealetimesheet.data.model.Shift
import nz.co.bealetimesheet.data.model.ShiftWithBreaks
import nz.co.bealetimesheet.data.model.TimesheetDayWithShifts
import nz.co.bealetimesheet.ui.settings.SettingsRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun CurrentTimesheetScreen(
    weekStarting: String,
    days: List<TimesheetDayWithShifts>,
    isLoading: Boolean,
    errorMessage: String?,
    isEditable: Boolean = true,
    isSubmitted: Boolean = false,
    onExport: (() -> Unit)? = null,
    onUnlock: (() -> Unit)? = null,
    onAddShift: (
        date: String,
        startTime: String,
        finishTime: String?,
        onSuccess: () -> Unit
    ) -> Unit,
    onUpdateShift: (
        shift: Shift,
        startTime: String,
        finishTime: String?,
        comments: String,
        onSuccess: () -> Unit
    ) -> Unit,
    onDeleteShift: (Shift, () -> Unit) -> Unit,
    onAddRestBreak: (
        shift: Shift,
        startTime: String,
        finishTime: String,
        onSuccess: () -> Unit
    ) -> Unit,
    onUpdateRestBreak: (
        restBreak: RestBreak,
        startTime: String,
        finishTime: String?,
        onSuccess: () -> Unit
    ) -> Unit,
    onDeleteRestBreak: (RestBreak, () -> Unit) -> Unit,
    onBack: () -> Unit
) {
    val weekStart = runCatching { LocalDate.parse(weekStarting) }.getOrNull()
    val daysByDate = days.associateBy { it.day.date }
    var editingShift by remember { mutableStateOf<ShiftWithBreaks?>(null) }
    var editingDay by remember {
        mutableStateOf<TimesheetDayWithShifts?>(null)
    }
    var editingBreak by remember { mutableStateOf<RestBreak?>(null) }
    var addingShiftDate by remember { mutableStateOf<LocalDate?>(null) }
    var addingBreakToShift by remember {
        mutableStateOf<Shift?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Current Timesheet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tap Edit to correct shifts, breaks, or comments.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (isSubmitted) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (isEditable) {
                            "SUBMITTED — UNLOCKED FOR CORRECTIONS"
                        } else {
                            "SUBMITTED — LOCKED"
                        },
                        fontWeight = FontWeight.Bold
                    )
                    onUnlock?.let { unlock ->
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = unlock) {
                            Text("Unlock for Corrections")
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        errorMessage?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        when {
            isLoading -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            weekStart == null -> {
                Text("Unable to determine the current pay week.")
            }

            else -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(7) { offset ->
                        val date = weekStart.plusDays(offset.toLong())
                        val day = daysByDate[date.toString()]
                        DayCard(
                            date = date,
                            day = day,
                            onAddShift = if (isEditable) {
                                {
                                    addingShiftDate = date
                                }
                            } else {
                                null
                            },
                            onEditShift = if (isEditable) {
                                { shiftWithBreaks ->
                                    editingDay = day
                                    editingShift = shiftWithBreaks
                                }
                            } else {
                                null
                            },
                            onAddBreak = if (isEditable) {
                                { shift ->
                                    addingBreakToShift = shift
                                }
                            } else {
                                null
                            },
                            onEditBreak = if (isEditable) {
                                { restBreak ->
                                    editingBreak = restBreak
                                }
                            } else {
                                null
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        onExport?.let { export ->
            Button(
                onClick = export,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Re-export & Email This Week")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }

    editingShift?.let { shiftWithBreaks ->
        ShiftEditorDialog(
            shiftWithBreaks = shiftWithBreaks,
            initialComments = editingDay?.day?.comments.orEmpty(),
            onDismiss = {
                editingShift = null
                editingDay = null
            },
            onSave = { startTime, finishTime, comments ->
                onUpdateShift(
                    shiftWithBreaks.shift,
                    startTime,
                    finishTime,
                    comments
                ) {
                    editingShift = null
                    editingDay = null
                }
            },
            onDelete = {
                onDeleteShift(shiftWithBreaks.shift) {
                    editingShift = null
                    editingDay = null
                }
            }
        )
    }

    editingBreak?.let { restBreak ->
        BreakEditorDialog(
            restBreak = restBreak,
            onDismiss = { editingBreak = null },
            onSave = { startTime, finishTime ->
                onUpdateRestBreak(
                    restBreak,
                    startTime,
                    finishTime
                ) {
                    editingBreak = null
                }
            },
            onDelete = {
                onDeleteRestBreak(restBreak) {
                    editingBreak = null
                }
            }
        )
    }

    addingShiftDate?.let { date ->
        AddShiftDialog(
            date = date,
            onDismiss = { addingShiftDate = null },
            onSave = { startTime, finishTime ->
                onAddShift(
                    date.toString(),
                    startTime,
                    finishTime
                ) {
                    addingShiftDate = null
                }
            }
        )
    }

    addingBreakToShift?.let { shift ->
        AddBreakDialog(
            shiftNumber = shift.shiftNumber,
            onDismiss = { addingBreakToShift = null },
            onSave = { startTime, finishTime ->
                onAddRestBreak(
                    shift,
                    startTime,
                    finishTime
                ) {
                    addingBreakToShift = null
                }
            }
        )
    }
}

@Composable
private fun DayCard(
    date: LocalDate,
    day: TimesheetDayWithShifts?,
    onAddShift: (() -> Unit)?,
    onEditShift: ((ShiftWithBreaks) -> Unit)?,
    onAddBreak: ((Shift) -> Unit)?,
    onEditBreak: ((RestBreak) -> Unit)?
) {
    val headingFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM")
    val shifts = day?.shifts
        ?.sortedBy { it.shift.shiftNumber }
        .orEmpty()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = date.format(headingFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (shifts.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No shifts recorded",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            shifts.forEach { shiftWithBreaks ->
                Spacer(modifier = Modifier.height(12.dp))
                ShiftRow(
                    shiftWithBreaks = shiftWithBreaks,
                    onAddBreak = onAddBreak?.let { addBreak ->
                        {
                            addBreak(shiftWithBreaks.shift)
                        }
                    },
                    onEditShift = onEditShift?.let { edit ->
                        { edit(shiftWithBreaks) }
                    },
                    onEditBreak = onEditBreak
                )
            }

            day?.day?.comments
                ?.takeIf { it.isNotBlank() }
                ?.let { comments ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Comments",
                        fontWeight = FontWeight.Bold
                    )
                    Text(comments)
                }

            if (shifts.size < 3 && onAddShift != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onAddShift,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (shifts.isEmpty()) {
                            "Add Shift"
                        } else {
                            "Add Another Shift"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddShiftDialog(
    date: LocalDate,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    var startTime by rememberSaveable { mutableStateOf("") }
    var finishTime by rememberSaveable { mutableStateOf("") }
    val headingFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Shift") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(date.format(headingFormatter))
                Text(
                    "Select the shift times. Leave finish blank only " +
                        "if this will be the currently active shift."
                )
                TimePickerButton(
                    label = "Start time",
                    value = startTime,
                    onTimeSelected = { startTime = it }
                )
                TimePickerButton(
                    label = "Finish time",
                    value = finishTime,
                    allowBlank = true,
                    onTimeSelected = { finishTime = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        startTime.trim(),
                        finishTime.trim().ifBlank { null }
                    )
                }
            ) {
                Text("Add Shift")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ShiftRow(
    shiftWithBreaks: ShiftWithBreaks,
    onEditShift: (() -> Unit)?,
    onAddBreak: (() -> Unit)?,
    onEditBreak: ((RestBreak) -> Unit)?
) {
    val shift = shiftWithBreaks.shift
    val use24HourTime = SettingsRepository.getUse24HourTime(
        LocalContext.current
    )
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Shift ${shift.shiftNumber}",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text =
                        "${displayTime(shift.startTime, use24HourTime)} – " +
                            (shift.finishTime?.let {
                                displayTime(it, use24HourTime)
                            } ?: "Active")
                )
            }
            onEditShift?.let { edit ->
                OutlinedButton(onClick = edit) {
                    Text("Edit")
                }
            }
        }

        onAddBreak?.let { addBreak ->
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(onClick = addBreak) {
                Text("Add Break")
            }
        }

        shiftWithBreaks.restBreaks
            .sortedBy { it.startTime }
            .forEach { restBreak ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Break: ${
                            displayTime(
                                restBreak.startTime,
                                use24HourTime
                            )
                        } – " +
                            (restBreak.finishTime?.let {
                                displayTime(it, use24HourTime)
                            } ?: "In progress")
                    )
                    onEditBreak?.let { editBreak ->
                        TextButton(onClick = { editBreak(restBreak) }) {
                            Text("Edit")
                        }
                    }
                }
            }
    }
}

@Composable
private fun ShiftEditorDialog(
    shiftWithBreaks: ShiftWithBreaks,
    initialComments: String,
    onDismiss: () -> Unit,
    onSave: (String, String?, String) -> Unit,
    onDelete: () -> Unit
) {
    var startTime by rememberSaveable {
        mutableStateOf(shiftWithBreaks.shift.startTime)
    }
    var finishTime by rememberSaveable {
        mutableStateOf(shiftWithBreaks.shift.finishTime.orEmpty())
    }
    var comments by rememberSaveable { mutableStateOf(initialComments) }
    var confirmDelete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Shift ${shiftWithBreaks.shift.shiftNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select the shift times.")
                TimePickerButton(
                    label = "Start time",
                    value = startTime,
                    onTimeSelected = { startTime = it }
                )
                TimePickerButton(
                    label = "Finish time",
                    value = finishTime,
                    allowBlank = true,
                    onTimeSelected = { finishTime = it }
                )
                OutlinedTextField(
                    value = comments,
                    onValueChange = { comments = it },
                    label = { Text("Day comments") },
                    minLines = 2
                )
                TextButton(onClick = { confirmDelete = true }) {
                    Text(
                        text = "Delete Shift",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        startTime.trim(),
                        finishTime.trim().ifBlank { null },
                        comments.trim()
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (confirmDelete) {
        DeleteConfirmationDialog(
            itemName = "this shift and all its breaks",
            onDismiss = { confirmDelete = false },
            onConfirm = onDelete
        )
    }
}

@Composable
private fun AddBreakDialog(
    shiftNumber: Int,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var startTime by rememberSaveable { mutableStateOf("") }
    var finishTime by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Break to Shift $shiftNumber") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select the break start and finish times.")
                TimePickerButton(
                    label = "Break start",
                    value = startTime,
                    onTimeSelected = { startTime = it }
                )
                TimePickerButton(
                    label = "Break finish",
                    value = finishTime,
                    onTimeSelected = { finishTime = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        startTime.trim(),
                        finishTime.trim()
                    )
                },
                enabled =
                    startTime.isNotBlank() &&
                        finishTime.isNotBlank()
            ) {
                Text("Add Break")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BreakEditorDialog(
    restBreak: RestBreak,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit,
    onDelete: () -> Unit
) {
    var startTime by rememberSaveable {
        mutableStateOf(restBreak.startTime)
    }
    var finishTime by rememberSaveable {
        mutableStateOf(restBreak.finishTime.orEmpty())
    }
    var confirmDelete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Rest Break") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select the break times. Finish may stay blank if active.")
                TimePickerButton(
                    label = "Break start",
                    value = startTime,
                    onTimeSelected = { startTime = it }
                )
                TimePickerButton(
                    label = "Break finish",
                    value = finishTime,
                    allowBlank = true,
                    onTimeSelected = { finishTime = it }
                )
                TextButton(onClick = { confirmDelete = true }) {
                    Text(
                        text = "Delete Rest Break",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        startTime.trim(),
                        finishTime.trim().ifBlank { null }
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (confirmDelete) {
        DeleteConfirmationDialog(
            itemName = "this rest break",
            onDismiss = { confirmDelete = false },
            onConfirm = onDelete
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    itemName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm deletion") },
        text = { Text("Delete $itemName? This cannot be undone.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TimePickerButton(
    label: String,
    value: String,
    allowBlank: Boolean = false,
    onTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val use24HourTime = SettingsRepository.getUse24HourTime(context)
    val selectedTime = runCatching {
        LocalTime.parse(value)
    }.getOrElse {
        LocalTime.now()
    }

    Column {
        OutlinedButton(
            onClick = {
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        onTimeSelected(
                            LocalTime.of(hour, minute).format(
                                DateTimeFormatter.ofPattern("HH:mm")
                            )
                        )
                    },
                    selectedTime.hour,
                    selectedTime.minute,
                    use24HourTime
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (value.isBlank()) {
                    "$label: Select time"
                } else {
                    "$label: ${displayTime(value, use24HourTime)}"
                }
            )
        }

        if (allowBlank && value.isNotBlank()) {
            TextButton(
                onClick = { onTimeSelected("") }
            ) {
                Text("Clear $label")
            }
        }
    }
}

private fun displayTime(
    value: String,
    use24HourTime: Boolean
): String {
    return runCatching {
        LocalTime.parse(value).format(
            DateTimeFormatter.ofPattern(
                if (use24HourTime) "HH:mm" else "h:mm a"
            )
        )
    }.getOrDefault(value)
}
