package nz.co.bealetimesheet.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import nz.co.bealetimesheet.data.model.TimesheetDayWithShifts
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import nz.co.bealetimesheet.R

private enum class DashboardStatus {
    OFF_SHIFT,
    ON_SHIFT,
    ON_BREAK
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    tuesdayReminderEnabled: Boolean,
    activeShiftReminderEnabled: Boolean,
    onStartShift: () -> Unit,
    onStartRestBreak: () -> Unit,
    onFinishRestBreak: () -> Unit,
    onEndShift: () -> Unit,
    onCurrentTimesheet: () -> Unit,
    onTimesheetHistory: () -> Unit,
    onExportAndEmail: () -> Unit,
    onBackupRestore: () -> Unit,
    onSettings: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEE d MMM")
    val today = LocalDate.now()
    val weekStarting = today.with(
        TemporalAdjusters.previousOrSame(DayOfWeek.WEDNESDAY)
    )
    val weekEnding = weekStarting.plusDays(6)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.splash_logo),
            contentDescription = "R&L Beale Log Transport LTD",
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Timesheet Dashboard",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${weekStarting.format(dateFormatter)} – " +
                weekEnding.format(dateFormatter),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (
            tuesdayReminderEnabled &&
            today.dayOfWeek == DayOfWeek.TUESDAY &&
            !uiState.currentWeekIsSubmitted
        ) {
            ReminderCard(
                title = "Tuesday Timesheet Reminder",
                message = "Review, sign, export and submit this pay week " +
                    "after your final shift."
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (
            activeShiftReminderEnabled &&
            uiState.activeShift != null
        ) {
            ReminderCard(
                title = "Active Shift Reminder",
                message = "A shift is still active. Check that this is " +
                    "correct before leaving the app."
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (uiState.isLoading) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading timesheet...")
                }
            }
        } else {
            ShiftDashboardCard(
                uiState = uiState,
                onStartShift = onStartShift,
                onStartRestBreak = onStartRestBreak,
                onFinishRestBreak = onFinishRestBreak,
                onEndShift = onEndShift
            )
        }

        uiState.errorMessage?.let { errorMessage ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Current Pay Week",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Review or correct any shift in the current " +
                        "Wednesday–Tuesday pay week."
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCurrentTimesheet,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Current Timesheet")
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onTimesheetHistory,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Timesheet History")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Timesheet Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onExportAndEmail,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export & Email")
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Settings")
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onBackupRestore,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Backup & Restore")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Current pay-week entries remain editable until submission.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ReminderCard(
    title: String,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun ShiftDashboardCard(
    uiState: HomeUiState,
    onStartShift: () -> Unit,
    onStartRestBreak: () -> Unit,
    onFinishRestBreak: () -> Unit,
    onEndShift: () -> Unit
) {
    val activeShift = uiState.activeShift
    val activeRestBreak = uiState.activeRestBreak
    val status = when {
        activeShift == null -> DashboardStatus.OFF_SHIFT
        activeRestBreak != null -> DashboardStatus.ON_BREAK
        else -> DashboardStatus.ON_SHIFT
    }

    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(status, activeShift?.id, activeRestBreak?.id) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(30_000L)
        }
    }

    val statusColour = when (status) {
        DashboardStatus.OFF_SHIFT -> Color(0xFFC62828)
        DashboardStatus.ON_SHIFT -> Color(0xFF2E7D32)
        DashboardStatus.ON_BREAK -> Color(0xFFF9A825)
    }
    val statusBackground = when (status) {
        DashboardStatus.OFF_SHIFT -> Color(0xFFFFEBEE)
        DashboardStatus.ON_SHIFT -> Color(0xFFE8F5E9)
        DashboardStatus.ON_BREAK -> Color(0xFFFFF8E1)
    }
    val statusText = when (status) {
        DashboardStatus.OFF_SHIFT -> "OFF SHIFT"
        DashboardStatus.ON_SHIFT -> "ON SHIFT"
        DashboardStatus.ON_BREAK -> "ON BREAK"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = statusBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(statusColour, CircleShape)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColour
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            val todayMinutes = uiState.currentWeekDays
                .filter { it.day.date == LocalDate.now().toString() }
                .sumOf { calculateWorkedMinutes(it, currentTime) }
            val weekMinutes = uiState.currentWeekDays.sumOf {
                calculateWorkedMinutes(it, currentTime)
            }

            DashboardValueRow(
                label = "Today's Hours",
                value = formatMinutes(todayMinutes)
            )
            Spacer(modifier = Modifier.height(10.dp))
            DashboardValueRow(
                label = "Pay-Week Hours",
                value = formatMinutes(weekMinutes)
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (activeShift == null) {
                Text(
                    text = "No shift is currently active.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(22.dp))
                Button(
                    onClick = onStartShift,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Start Shift",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                return@Column
            }

            val shiftStart = parseStoredTime(activeShift.startTime)
            DashboardValueRow(
                label = "Shift started",
                value = formatStoredTime(activeShift.startTime)
            )
            Spacer(modifier = Modifier.height(10.dp))
            DashboardValueRow(
                label = "Current shift",
                value = elapsedTime(shiftStart, currentTime)
            )

            if (activeRestBreak != null) {
                val breakStart = parseStoredTime(activeRestBreak.startTime)
                Spacer(modifier = Modifier.height(10.dp))
                DashboardValueRow(
                    label = "Break started",
                    value = formatStoredTime(activeRestBreak.startTime)
                )
                Spacer(modifier = Modifier.height(10.dp))
                DashboardValueRow(
                    label = "Break duration",
                    value = elapsedTime(breakStart, currentTime)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onFinishRestBreak,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Finish Rest Break",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onStartRestBreak,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "Start Rest Break",
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onEndShift,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "End Shift",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardValueRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun parseStoredTime(value: String): LocalTime? {
    return runCatching { LocalTime.parse(value) }.getOrNull()
}

private fun formatStoredTime(value: String): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    return parseStoredTime(value)?.format(formatter) ?: value
}

private fun elapsedTime(
    startTime: LocalTime?,
    currentTime: LocalDateTime
): String {
    if (startTime == null) {
        return "—"
    }

    var startDateTime = LocalDateTime.of(currentTime.toLocalDate(), startTime)
    if (startDateTime.isAfter(currentTime)) {
        startDateTime = startDateTime.minusDays(1)
    }

    val totalMinutes = Duration.between(
        startDateTime,
        currentTime
    ).toMinutes().coerceAtLeast(0L)

    return "${totalMinutes / 60}h " +
        "${(totalMinutes % 60).toString().padStart(2, '0')}m"
}

private fun calculateWorkedMinutes(
    day: TimesheetDayWithShifts,
    currentTime: LocalDateTime
): Long {
    val dayDate = runCatching {
        LocalDate.parse(day.day.date)
    }.getOrNull() ?: return 0L

    return day.shifts.sumOf { shiftWithBreaks ->
        val shift = shiftWithBreaks.shift
        val startTime = parseStoredTime(shift.startTime)
            ?: return@sumOf 0L
        val shiftStart = LocalDateTime.of(dayDate, startTime)
        val shiftFinish = shift.finishTime?.let { finishText ->
            parseStoredTime(finishText)?.let { finishTime ->
                var finish = LocalDateTime.of(dayDate, finishTime)
                if (finish.isBefore(shiftStart)) {
                    finish = finish.plusDays(1)
                }
                finish
            }
        } ?: currentTime

        if (shiftFinish.isBefore(shiftStart)) {
            return@sumOf 0L
        }

        val shiftMinutes = Duration.between(
            shiftStart,
            shiftFinish
        ).toMinutes()

        val breakMinutes = shiftWithBreaks.restBreaks.sumOf { restBreak ->
            val breakStartTime = parseStoredTime(restBreak.startTime)
                ?: return@sumOf 0L
            var breakStart = LocalDateTime.of(dayDate, breakStartTime)
            if (breakStart.isBefore(shiftStart)) {
                breakStart = breakStart.plusDays(1)
            }

            val breakFinish = restBreak.finishTime?.let { finishText ->
                parseStoredTime(finishText)?.let { finishTime ->
                    var finish = LocalDateTime.of(
                        breakStart.toLocalDate(),
                        finishTime
                    )
                    if (finish.isBefore(breakStart)) {
                        finish = finish.plusDays(1)
                    }
                    finish
                }
            } ?: currentTime

            if (breakFinish.isBefore(breakStart)) {
                0L
            } else {
                Duration.between(
                    breakStart,
                    minOf(breakFinish, shiftFinish)
                ).toMinutes().coerceAtLeast(0L)
            }
        }

        (shiftMinutes - breakMinutes).coerceAtLeast(0L)
    }
}

private fun formatMinutes(totalMinutes: Long): String {
    val safeMinutes = totalMinutes.coerceAtLeast(0L)
    return "${safeMinutes / 60}h " +
        "${(safeMinutes % 60).toString().padStart(2, '0')}m"
}
