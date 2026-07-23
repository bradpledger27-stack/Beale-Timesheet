package nz.co.bealetimesheet.ui.timesheet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nz.co.bealetimesheet.data.database.BealeDatabase
import nz.co.bealetimesheet.data.model.TimesheetEntry
import nz.co.bealetimesheet.data.repository.TimesheetRepository
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class TimesheetDay(
    val date: LocalDate,
    val entry: TimesheetEntry? = null,
    val hoursWorked: Double = 0.0
)

data class TimesheetUiState(
    val isLoading: Boolean = true,
    val weekStart: LocalDate = currentPayWeekStart(),
    val weekEnd: LocalDate = currentPayWeekStart().plusDays(6),
    val days: List<TimesheetDay> = emptyList(),
    val totalHours: Double = 0.0,
    val errorMessage: String? = null
)

class TimesheetViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = BealeDatabase.getDatabase(application)

    private val repository = TimesheetRepository(
        database.timesheetDao()
    )

    private val _uiState = MutableStateFlow(
        TimesheetUiState()
    )

    val uiState: StateFlow<TimesheetUiState> =
        _uiState.asStateFlow()

    init {
        loadCurrentWeek()
    }

    private fun loadCurrentWeek() {
        viewModelScope.launch {
            try {
                repository.getAllEntries().collect { entries ->

                    val weekStart = currentPayWeekStart()
                    val weekEnd = weekStart.plusDays(6)

                    val weekDays = (0L..6L).map { dayOffset ->
                        val date = weekStart.plusDays(dayOffset)

                        val entry = entries.firstOrNull {
                            it.date == date.toString()
                        }

                        TimesheetDay(
                            date = date,
                            entry = entry,
                            hoursWorked = entry?.let {
                                calculateHoursWorked(it)
                            } ?: 0.0
                        )
                    }

                    _uiState.value = TimesheetUiState(
                        isLoading = false,
                        weekStart = weekStart,
                        weekEnd = weekEnd,
                        days = weekDays,
                        totalHours = weekDays.sumOf {
                            it.hoursWorked
                        },
                        errorMessage = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                        ?: "Unable to load the current timesheet"
                )
            }
        }
    }
}

private fun currentPayWeekStart(
    date: LocalDate = LocalDate.now()
): LocalDate {

    var result = date

    while (result.dayOfWeek != DayOfWeek.WEDNESDAY) {
        result = result.minusDays(1)
    }

    return result
}

private fun calculateHoursWorked(
    entry: TimesheetEntry
): Double {

    return try {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        val start = LocalTime.parse(
            entry.startTime,
            formatter
        )

        val finish = LocalTime.parse(
            entry.finishTime,
            formatter
        )

        var workedMinutes = Duration.between(
            start,
            finish
        ).toMinutes()

        if (workedMinutes < 0) {
            workedMinutes += 24 * 60
        }

        val breakStart = entry.breakStartTime
        val breakFinish = entry.breakFinishTime

        if (
            !breakStart.isNullOrBlank() &&
            !breakFinish.isNullOrBlank()
        ) {
            val breakStartTime = LocalTime.parse(
                breakStart,
                formatter
            )

            val breakFinishTime = LocalTime.parse(
                breakFinish,
                formatter
            )

            var breakMinutes = Duration.between(
                breakStartTime,
                breakFinishTime
            ).toMinutes()

            if (breakMinutes < 0) {
                breakMinutes += 24 * 60
            }

            workedMinutes -= breakMinutes
        }

        if (workedMinutes < 0) {
            0.0
        } else {
            workedMinutes / 60.0
        }
    } catch (_: Exception) {
        0.0
    }
}