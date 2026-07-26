package nz.co.bealetimesheet.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import nz.co.bealetimesheet.data.model.RestBreak
import nz.co.bealetimesheet.data.model.Shift
import nz.co.bealetimesheet.data.model.TimesheetDayWithShifts
import nz.co.bealetimesheet.data.repository.TimesheetRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class HomeUiState(
    val isLoading: Boolean = true,
    val activeShift: Shift? = null,
    val activeRestBreak: RestBreak? = null,
    val currentWeekDays: List<TimesheetDayWithShifts> = emptyList(),
    val currentWeekIsSubmitted: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val repository: TimesheetRepository,
    private val employeeNameProvider: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeCurrentWeek()
        refreshActiveShift()
    }

    private fun observeCurrentWeek() {
        val weekStarting = LocalDate.now().with(
            TemporalAdjusters.previousOrSame(DayOfWeek.WEDNESDAY)
        )

        viewModelScope.launch {
            repository.observeWeek(
                weekStarting = weekStarting.toString(),
                weekEnding = weekStarting.plusDays(6).toString()
            ).collectLatest { days ->
                _uiState.value = _uiState.value.copy(
                    currentWeekDays = days
                )
            }
        }

        viewModelScope.launch {
            repository.observeWeekRecord(weekStarting.toString())
                .collectLatest { week ->
                    _uiState.value = _uiState.value.copy(
                        currentWeekIsSubmitted =
                            week?.isSubmitted == true
                    )
                }
        }
    }

    fun refreshActiveShift() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                refreshState()
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                        ?: "Unable to load the current shift."
                )
            }
        }
    }

    fun startShift(
        weekStarting: String,
        date: String,
        startTime: String,
        onSuccess: () -> Unit
    ) {
        runAction(
            defaultErrorMessage = "Unable to start the shift.",
            action = {
                val employeeName = employeeNameProvider().trim()

                require(employeeName.isNotBlank()) {
                    "Please enter an employee name in Settings before starting a shift."
                }

                repository.startShift(
                    employeeName = employeeName,
                    weekStarting = weekStarting,
                    date = date,
                    startTime = startTime
                )
            },
            onSuccess = onSuccess
        )
    }

    fun startRestBreak(
        startTime: String,
        onSuccess: () -> Unit = {}
    ) {
        runAction(
            defaultErrorMessage = "Unable to start the rest break.",
            action = {
                val activeShift = repository.getActiveShift()
                    ?: error("There is no active shift.")

                repository.startRestBreak(
                    shiftId = activeShift.id,
                    startTime = startTime
                )
            },
            onSuccess = onSuccess
        )
    }

    fun finishRestBreak(
        finishTime: String,
        onSuccess: () -> Unit = {}
    ) {
        runAction(
            defaultErrorMessage = "Unable to finish the rest break.",
            action = {
                val activeShift = repository.getActiveShift()
                    ?: error("There is no active shift.")

                repository.finishRestBreak(
                    shiftId = activeShift.id,
                    finishTime = finishTime
                )
            },
            onSuccess = onSuccess
        )
    }

    fun addRestBreak(
        breakStartTime: String,
        breakFinishTime: String,
        onSuccess: () -> Unit
    ) {
        runAction(
            defaultErrorMessage = "Unable to save the rest break.",
            action = {
                val activeShift = repository.getActiveShift()
                    ?: error("There is no active shift.")

                repository.addRestBreak(
                    shiftId = activeShift.id,
                    startTime = breakStartTime,
                    finishTime = breakFinishTime
                )
            },
            onSuccess = onSuccess
        )
    }

    fun finishShift(
        finishTime: String,
        comments: String,
        onSuccess: () -> Unit
    ) {
        runAction(
            defaultErrorMessage = "Unable to finish the shift.",
            action = {
                val activeShift = repository.getActiveShift()
                    ?: error("There is no active shift.")

                repository.finishShift(
                    shiftId = activeShift.id,
                    finishTime = finishTime,
                    comments = comments
                )
            },
            onSuccess = onSuccess
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }

    private fun runAction(
        defaultErrorMessage: String,
        action: suspend () -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                action()
                refreshState()
                onSuccess()
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                        ?: defaultErrorMessage
                )
            }
        }
    }

    private suspend fun refreshState() {
        val activeShift = repository.getActiveShift()
        val activeRestBreak = activeShift?.let { shift ->
            repository.getActiveRestBreak(shift.id)
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            activeShift = activeShift,
            activeRestBreak = activeRestBreak,
            errorMessage = null
        )
    }
}
