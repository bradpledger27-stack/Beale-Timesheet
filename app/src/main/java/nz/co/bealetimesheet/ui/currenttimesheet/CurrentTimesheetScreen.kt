package nz.co.bealetimesheet.ui.currenttimesheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nz.co.bealetimesheet.data.model.ShiftWithBreaks
import nz.co.bealetimesheet.data.model.TimesheetDayWithShifts
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val GridLineColor = Color(0xFF222222)
private val HeaderBackgroundColor = Color(0xFFE8E8E8)
private val DayBackgroundColor = Color(0xFFF4F4F4)
private val PaperBackgroundColor = Color.White

private val DayColumnWidth = 120.dp
private val ShiftColumnWidth = 135.dp
private val BreakColumnWidth = 185.dp
private val CommentsColumnWidth = 260.dp

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

    val weekEndDate = weekStartDate?.plusDays(6)

    val weekHeadingFormatter = DateTimeFormatter.ofPattern(
        "dd MMMM yyyy"
    )

    val weekDates = if (weekStartDate != null) {
        (0L..6L).map { offset ->
            weekStartDate.plusDays(offset)
        }
    } else {
        emptyList()
    }

    val daysByDate = days.associateBy {
        it.day.date
    }

    val employeeName = days
        .firstOrNull()
        ?.day
        ?.employeeName
        ?.takeIf { it.isNotBlank() }
        ?: "Not recorded"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        Text(
            text = "Current Timesheet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            weekStartDate == null || weekEndDate == null -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Unable to determine the current pay week.",
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = GridLineColor
                    ),
                    color = PaperBackgroundColor
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(
                                rememberScrollState()
                            )
                    ) {
                        TimesheetTitleSection(
                            employeeName = employeeName,
                            weekStartText = weekStartDate.format(
                                weekHeadingFormatter
                            ),
                            weekEndText = weekEndDate.format(
                                weekHeadingFormatter
                            )
                        )

                        Column(
                            modifier = Modifier.horizontalScroll(
                                rememberScrollState()
                            )
                        ) {
                            TimesheetTableHeader()

                            weekDates.forEach { date ->
                                TimesheetDaySection(
                                    date = date,
                                    dayWithShifts = daysByDate[
                                        date.toString()
                                    ]
                                )
                            }

                            SignatureSection()
                        }
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
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
private fun TimesheetTitleSection(
    employeeName: String,
    weekStartText: String,
    weekEndText: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaperBackgroundColor)
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "BEALE LOGGERS",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = "EMPLOYEE TIMESHEET",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(min = 840.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LabelledValueBox(
                label = "Employee",
                value = employeeName,
                modifier = Modifier.weight(1f)
            )

            LabelledValueBox(
                label = "Pay Week",
                value = "$weekStartText to $weekEndText",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LabelledValueBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                color = GridLineColor
            )
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderBackgroundColor)
                .padding(
                    horizontal = 8.dp,
                    vertical = 4.dp
                ),
            color = Color.Black,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = value,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp,
                    vertical = 8.dp
                ),
            color = Color.Black,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun TimesheetTableHeader() {
    Row(
        modifier = Modifier.width(
            DayColumnWidth +
                    ShiftColumnWidth +
                    BreakColumnWidth +
                    CommentsColumnWidth
        )
    ) {
        HeaderCell(
            text = "DAY / DATE",
            width = DayColumnWidth
        )

        HeaderCell(
            text = "SHIFT TIMES",
            width = ShiftColumnWidth
        )

        HeaderCell(
            text = "REST BREAKS",
            width = BreakColumnWidth
        )

        HeaderCell(
            text = "COMMENTS",
            width = CommentsColumnWidth
        )
    }
}

@Composable
private fun HeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(44.dp)
            .background(HeaderBackgroundColor)
            .border(
                width = 0.5.dp,
                color = GridLineColor
            )
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TimesheetDaySection(
    date: LocalDate,
    dayWithShifts: TimesheetDayWithShifts?
) {
    val dateFormatter = DateTimeFormatter.ofPattern(
        "EEEE\ndd MMM"
    )

    val shifts = dayWithShifts
        ?.shifts
        ?.sortedBy { it.shift.shiftNumber }
        .orEmpty()

    val comments = dayWithShifts
        ?.day
        ?.comments
        .orEmpty()

    val rowHeight = 46.dp
    val sectionHeight = rowHeight * 3

    Row(
        modifier = Modifier
            .width(
                DayColumnWidth +
                        ShiftColumnWidth +
                        BreakColumnWidth +
                        CommentsColumnWidth
            )
            .height(sectionHeight)
    ) {
        Box(
            modifier = Modifier
                .width(DayColumnWidth)
                .height(sectionHeight)
                .background(DayBackgroundColor)
                .border(
                    width = 0.5.dp,
                    color = GridLineColor
                )
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.format(dateFormatter),
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )
        }

        Column(
            modifier = Modifier
                .width(ShiftColumnWidth)
                .height(sectionHeight)
        ) {
            repeat(3) { index ->
                val shiftWithBreaks = shifts.getOrNull(index)

                ShiftTimeCell(
                    shiftWithBreaks = shiftWithBreaks,
                    modifier = Modifier.height(rowHeight)
                )
            }
        }

        Column(
            modifier = Modifier
                .width(BreakColumnWidth)
                .height(sectionHeight)
        ) {
            repeat(3) { index ->
                val shiftWithBreaks = shifts.getOrNull(index)

                BreakTimeCell(
                    shiftWithBreaks = shiftWithBreaks,
                    modifier = Modifier.height(rowHeight)
                )
            }
        }

        Box(
            modifier = Modifier
                .width(CommentsColumnWidth)
                .height(sectionHeight)
                .border(
                    width = 0.5.dp,
                    color = GridLineColor
                )
                .padding(8.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = comments,
                color = Color.Black,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ShiftTimeCell(
    shiftWithBreaks: ShiftWithBreaks?,
    modifier: Modifier = Modifier
) {
    val shiftText = if (shiftWithBreaks == null) {
        ""
    } else {
        val start = formatStoredTime(
            shiftWithBreaks.shift.startTime
        )

        val finish = shiftWithBreaks.shift.finishTime?.let {
            formatStoredTime(it)
        } ?: "Active"

        "$start - $finish"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                color = GridLineColor
            )
            .padding(
                horizontal = 6.dp,
                vertical = 4.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = shiftText,
            color = Color.Black,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun BreakTimeCell(
    shiftWithBreaks: ShiftWithBreaks?,
    modifier: Modifier = Modifier
) {
    val breakText = shiftWithBreaks
        ?.restBreaks
        ?.sortedBy { it.startTime }
        ?.joinToString(
            separator = "\n"
        ) { restBreak ->
            val start = formatStoredTime(
                restBreak.startTime
            )

            val finish = formatStoredTime(
                restBreak.finishTime
            )

            "$start - $finish"
        }
        .orEmpty()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                color = GridLineColor
            )
            .padding(
                horizontal = 6.dp,
                vertical = 3.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = breakText,
            color = Color.Black,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}

@Composable
private fun SignatureSection() {
    Row(
        modifier = Modifier
            .width(
                DayColumnWidth +
                        ShiftColumnWidth +
                        BreakColumnWidth +
                        CommentsColumnWidth
            )
            .heightIn(min = 82.dp)
    ) {
        Column(
            modifier = Modifier
                .width(
                    DayColumnWidth +
                            ShiftColumnWidth +
                            BreakColumnWidth
                )
                .height(82.dp)
                .border(
                    width = 0.5.dp,
                    color = GridLineColor
                )
                .padding(8.dp)
        ) {
            Text(
                text = "Employee Signature",
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(34.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GridLineColor)
            )
        }

        Column(
            modifier = Modifier
                .width(CommentsColumnWidth)
                .height(82.dp)
                .border(
                    width = 0.5.dp,
                    color = GridLineColor
                )
                .padding(8.dp)
        ) {
            Text(
                text = "Date",
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(34.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GridLineColor)
            )
        }
    }
}

private fun formatStoredTime(
    storedTime: String
): String {
    val storageFormatter = DateTimeFormatter.ofPattern(
        "HH:mm"
    )

    val displayFormatter = DateTimeFormatter.ofPattern(
        "h:mm a"
    )

    return runCatching {
        LocalTime.parse(
            storedTime,
            storageFormatter
        ).format(displayFormatter)
    }.getOrDefault(storedTime)
}