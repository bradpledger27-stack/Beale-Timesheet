package nz.co.bealetimesheet.ui.history

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nz.co.bealetimesheet.data.model.TimesheetWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TimesheetHistoryScreen(
    weekStarts: List<String>,
    weekRecords: List<TimesheetWeek>,
    currentWeekStarting: String,
    onOpenWeek: (String) -> Unit,
    onBack: () -> Unit
) {
    val recordsByWeek = weekRecords.associateBy { it.weekStarting }
    val previousWeeks = weekStarts.filter {
        it != currentWeekStarting
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Timesheet History",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Open a previous pay week to view or re-export it.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (previousWeeks.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No previous pay weeks have been recorded yet.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(previousWeeks, key = { it }) { weekStarting ->
                    HistoryWeekCard(
                        weekStarting = weekStarting,
                        weekRecord = recordsByWeek[weekStarting],
                        onOpen = { onOpenWeek(weekStarting) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun HistoryWeekCard(
    weekStarting: String,
    weekRecord: TimesheetWeek?,
    onOpen: () -> Unit
) {
    val date = runCatching { LocalDate.parse(weekStarting) }.getOrNull()
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    val label = if (date == null) {
        "Week starting $weekStarting"
    } else {
        "${date.format(formatter)} – " +
            date.plusDays(6).format(formatter)
    }

    val status = when {
        weekRecord?.isSubmitted == true && weekRecord.isLocked ->
            "Submitted — Locked"

        weekRecord?.isSubmitted == true ->
            "Submitted — Unlocked for Corrections"

        else ->
            "In Progress"
    }

    val statusColor: Color = when {
        weekRecord?.isSubmitted == true && weekRecord.isLocked ->
            MaterialTheme.colorScheme.primary

        weekRecord?.isSubmitted == true ->
            MaterialTheme.colorScheme.tertiary

        else ->
            MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpen
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Wednesday–Tuesday",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = status,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
