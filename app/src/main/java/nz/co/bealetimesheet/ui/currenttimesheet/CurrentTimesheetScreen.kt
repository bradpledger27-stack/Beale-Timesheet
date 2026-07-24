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

    val daysByDate = days.associateBy { dayWithShifts ->
        dayWithShifts.day.date
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
