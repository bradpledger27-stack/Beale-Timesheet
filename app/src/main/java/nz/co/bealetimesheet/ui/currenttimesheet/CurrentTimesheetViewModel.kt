package nz.co.bealetimesheet.ui.currenttimesheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import nz.co.bealetimesheet.data.model.TimesheetDayWithShifts
import nz.co.bealetimesheet.data.model.RestBreak
import nz.co.bealetimesheet.data.model.Shift
import nz.co.bealetimesheet.data.repository.TimesheetRepository

data class CurrentTimesheetUiState(
    val isLoading: Boolean = true,
    val weekStarting: String = "",
    val days: List<TimesheetDayWithShifts> = emptyList(),
    val isSubmitted: Boolean = false,
    val isLocked: Boolean = false,
    val errorMessage: String? = null
)

class CurrentTimesheetViewModel(
    private val repository: TimesheetRepository,
    private val weekStarting: String,
    private val weekEnding: String,
    private val employeeNameProvider: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CurrentTimesheetUiState(
            isLoading = true,
            weekStarting = weekStarting
        )
    )

    val uiState: StateFlow<CurrentTimesheetUiState> =
        _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeWeek(
                weekStarting = weekStarting,
                weekEnding = weekEnding
            ).collectLatest { days ->

                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        days = days,
                        errorMessage = null
                    )
            }
        }
    }

    fun updateShift(
        shift: Shift,
        startTime: String,
        finishTime: String?,
        comments: String,
        onSuccess: () -> Unit
    ) {
        performUpdate("Unable to update the shift.", onSuccess) {
            repository.updateShiftTimes(
                shift = shift,
                startTime = startTime,
                finishTime = finishTime
            )
            repository.updateDayComments(
                dayId = shift.dayId,
                comments = comments
            )
        }
    }

    fun addShift(
        date: String,
        startTime: String,
        finishTime: String?,
        onSuccess: () -> Unit
    ) {
        performUpdate("Unable to add the shift.", onSuccess) {
            val employeeName = employeeNameProvider().trim()
            require(employeeName.isNotBlank()) {
                "Please enter an employee name in Settings first."
            }

            repository.addShift(
                employeeName = employeeName,
                weekStarting = weekStarting,
                date = date,
                startTime = startTime,
                finishTime = finishTime
            )
        }
    }

    fun deleteShift(
        shift: Shift,
        onSuccess: () -> Unit
    ) {
        performUpdate("Unable to delete the shift.", onSuccess) {
            repository.deleteShift(shift)
        }
    }

    fun updateRestBreak(
        restBreak: RestBreak,
        startTime: String,
        finishTime: String?,
        onSuccess: () -> Unit
    ) {
        performUpdate("Unable to update the rest break.", onSuccess) {
            require(startTime.isNotBlank()) {
                "Enter a break start time."
            }
            repository.updateRestBreak(
                restBreak.copy(
                    startTime = startTime,
                    finishTime = finishTime
                )
            )
        }
    }

    fun deleteRestBreak(
        restBreak: RestBreak,
        onSuccess: () -> Unit
    ) {
        performUpdate("Unable to delete the rest break.", onSuccess) {
            repository.deleteRestBreak(restBreak)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun performUpdate(
        defaultError: String,
        onSuccess: () -> Unit,
        update: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                update()
                onSuccess()
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = exception.message ?: defaultError
                )
            }
        }

        viewModelScope.launch {
            repository.observeWeekRecord(weekStarting)
                .collectLatest { week ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitted = week?.isSubmitted == true,
                        isLocked = week?.isLocked == true
                    )
                }
        }
    }
}

class CurrentTimesheetViewModelFactory(
    private val repository: TimesheetRepository,
    private val weekStarting: String,
    private val weekEnding: String,
    private val employeeNameProvider: () -> String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(CurrentTimesheetViewModel::class.java)) {
            return CurrentTimesheetViewModel(
                repository = repository,
                weekStarting = weekStarting,
                weekEnding = weekEnding,
                employeeNameProvider = employeeNameProvider
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}
