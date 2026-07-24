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
import nz.co.bealetimesheet.data.repository.TimesheetRepository

data class CurrentTimesheetUiState(
    val isLoading: Boolean = true,
    val weekStarting: String = "",
    val days: List<TimesheetDayWithShifts> = emptyList(),
    val errorMessage: String? = null
)

class CurrentTimesheetViewModel(
    private val repository: TimesheetRepository,
    private val weekStarting: String,
    private val weekEnding: String
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
}

class CurrentTimesheetViewModelFactory(
    private val repository: TimesheetRepository,
    private val weekStarting: String,
    private val weekEnding: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(CurrentTimesheetViewModel::class.java)) {
            return CurrentTimesheetViewModel(
                repository = repository,
                weekStarting = weekStarting,
                weekEnding = weekEnding
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}