package nz.co.bealetimesheet.ui.currenttimesheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nz.co.bealetimesheet.data.model.ShiftWithBreaks
import nz.co.bealetimesheet.data.model.TimesheetDayWithShifts
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CurrentTimesheetScreen(
    weekStarting: String,
    days: List<TimesheetDayWithShifts>,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit
) {
    val weekStartDate = runCatching {
        LocalDate.parse(weekStarting)
    }.getOrNull()

    val weekDateFormatter = DateTimeFormatter.ofPattern(
        "EEEE dd MMMM yyyy"
    )

    val dayDateFormatter = DateTimeFormatter.ofPattern(
        "EEEE dd MMMM"
    )

    val weekStartingText = if (weekStartDate != null) {
        weekStartDate.format(weekDateFormatter)
    } else {
        weekStarting
    }

    val weekDates = if (weekStartDate != null) {
        (0L..6L).map { dayOffset ->
            weekStartDate.plusDays(dayOffset)
        }
    } else {
        emptyList()
    }

    val daysByDate = days.associateBy {
        it.day.date
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Current Timesheet",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Week Starting: $weekStartingText",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        when {
            isLoading -> {
                Text("Loading timesheet...")
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            weekDates.isEmpty() -> {
                Text("Unable to determine the current pay week.")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = weekDates,
                        key = { date ->
                            date.toString()
                        }
                    ) { date ->
                        DayCard(
                            dateLabel = date.format(dayDateFormatter),
                            dayWithShifts = daysByDate[date.toString()]
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun DayCard(
    dateLabel: String,
    dayWithShifts: TimesheetDayWithShifts?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            if (dayWithShifts == null || dayWithShifts.shifts.isEmpty()) {
                Text(
                    text = "No shifts recorded",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                dayWithShifts.shifts
                    .sortedBy { it.shift.shiftNumber }
                    .forEachIndexed { index, shiftWithBreaks ->

                        ShiftDetails(shiftWithBreaks)

                        if (index < dayWithShifts.shifts.lastIndex) {
                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )
                        }
                    }

                if (dayWithShifts.day.comments.isNotBlank()) {
                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = "Comments",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        text = dayWithShifts.day.comments,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ShiftDetails(
    shiftWithBreaks: ShiftWithBreaks
) {
    val shift = shiftWithBreaks.shift
    val finishTimeText = shift.finishTime ?: "Active"

    Text(
        text = "finishTimeText",
        style = MaterialTheme.typography.titleMedium
    )

    if (shiftWithBreaks.restBreaks.isNotEmpty()) {
        Spacer(
            modifier = Modifier.height(4.dp)
        )

        shiftWithBreaks.restBreaks
            .sortedBy { restBreak ->
                restBreak.startTime
            }
            .forEach { restBreak ->
                Text(
                    text = "Rest break: " +
                            "${restBreak.startTime} - " +
                            restBreak.finishTime,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
    }
}