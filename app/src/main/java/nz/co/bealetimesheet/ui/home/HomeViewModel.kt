package nz.co.bealetimesheet.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nz.co.bealetimesheet.data.model.Shift
import nz.co.bealetimesheet.data.repository.TimesheetRepository

data class HomeUiState(
    val isLoading: Boolean = true,
    val activeShift: Shift? = null,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val repository: TimesheetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshActiveShift()
    }

    fun refreshActiveShift() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val activeShift = repository.getActiveShift()

                _uiState.value = HomeUiState(
                    isLoading = false,
                    activeShift = activeShift
                )
            } catch (exception: Exception) {
                _uiState.value = HomeUiState(
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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                repository.startShift(
                    employeeName = "Brad Pledger",
                    weekStarting = weekStarting,
                    date = date,
                    startTime = startTime
                )

                val activeShift = repository.getActiveShift()

                _uiState.value = HomeUiState(
                    isLoading = false,
                    activeShift = activeShift
                )

                onSuccess()
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                        ?: "Unable to start the shift."
                )
            }
        }
    }

    fun addRestBreak(
        breakStartTime: String,
        breakFinishTime: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val activeShift = repository.getActiveShift()
                    ?: error("There is no active shift.")

                repository.addRestBreak(
                    shiftId = activeShift.id,
                    startTime = breakStartTime,
                    finishTime = breakFinishTime
                )

                _uiState.value = HomeUiState(
                    isLoading = false,
                    activeShift = activeShift
                )

                onSuccess()
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                        ?: "Unable to save the rest break."
                )
            }
        }
    }

    fun finishShift(
        finishTime: String,
        comments: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val activeShift = repository.getActiveShift()
                    ?: error("There is no active shift.")

                repository.finishShift(
                    shiftId = activeShift.id,
                    finishTime = finishTime,
                    comments = comments
                )

                _uiState.value = HomeUiState(
                    isLoading = false,
                    activeShift = null
                )

                onSuccess()
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                        ?: "Unable to finish the shift."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }
}