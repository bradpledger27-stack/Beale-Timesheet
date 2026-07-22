package nz.co.bealetimesheet.ui.today

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
import java.time.LocalDate

data class TodayUiState(
    val isLoading: Boolean = true,
    val employeeName: String = "Brad Pledger",
    val date: String = LocalDate.now().toString(),
    val startTime: String = "",
    val finishTime: String = "",
    val breakStartTime: String? = null,
    val breakFinishTime: String? = null,
    val comments: String = "",
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

class TodayViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = BealeDatabase.getDatabase(application)
    private val repository = TimesheetRepository(database.timesheetDao())

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        loadTodayEntry()
    }

    private fun loadTodayEntry() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()

            try {
                val savedEntry = repository.getEntryByDate(today)

                _uiState.value = if (savedEntry == null) {
                    TodayUiState(
                        isLoading = false,
                        date = today
                    )
                } else {
                    TodayUiState(
                        isLoading = false,
                        employeeName = savedEntry.employeeName,
                        date = savedEntry.date,
                        startTime = savedEntry.startTime,
                        finishTime = savedEntry.finishTime,
                        breakStartTime = savedEntry.breakStartTime,
                        breakFinishTime = savedEntry.breakFinishTime,
                        comments = savedEntry.comments,
                        isSaved = true
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Unable to load today's entry"
                )
            }
        }
    }

    fun updateEmployeeName(value: String) {
        _uiState.value = _uiState.value.copy(
            employeeName = value,
            isSaved = false,
            errorMessage = null
        )
    }

    fun updateStartTime(value: String) {
        _uiState.value = _uiState.value.copy(
            startTime = value,
            isSaved = false,
            errorMessage = null
        )
    }

    fun updateFinishTime(value: String) {
        _uiState.value = _uiState.value.copy(
            finishTime = value,
            isSaved = false,
            errorMessage = null
        )
    }

    fun updateBreakTimes(
        startTime: String?,
        finishTime: String?
    ) {
        _uiState.value = _uiState.value.copy(
            breakStartTime = startTime,
            breakFinishTime = finishTime,
            isSaved = false,
            errorMessage = null
        )
    }

    fun removeBreak() {
        _uiState.value = _uiState.value.copy(
            breakStartTime = null,
            breakFinishTime = null,
            isSaved = false,
            errorMessage = null
        )
    }

    fun updateComments(value: String) {
        _uiState.value = _uiState.value.copy(
            comments = value,
            isSaved = false,
            errorMessage = null
        )
    }

    fun saveEntry() {
        val state = _uiState.value

        if (state.employeeName.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Please enter an employee name"
            )
            return
        }

        if (state.startTime.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Please choose a start time"
            )
            return
        }

        if (state.finishTime.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Please choose a finish time"
            )
            return
        }

        viewModelScope.launch {
            try {
                repository.saveEntry(
                    TimesheetEntry(
                        employeeName = state.employeeName.trim(),
                        date = state.date,
                        startTime = state.startTime,
                        finishTime = state.finishTime,
                        breakStartTime = state.breakStartTime,
                        breakFinishTime = state.breakFinishTime,
                        comments = state.comments.trim()
                    )
                )

                _uiState.value = _uiState.value.copy(
                    isSaved = true,
                    errorMessage = null
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaved = false,
                    errorMessage = exception.message ?: "Unable to save entry"
                )
            }
        }
    }
}