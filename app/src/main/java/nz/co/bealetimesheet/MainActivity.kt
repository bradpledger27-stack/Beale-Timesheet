package nz.co.bealetimesheet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import nz.co.bealetimesheet.data.database.BealeDatabase
import nz.co.bealetimesheet.data.repository.TimesheetRepository
import nz.co.bealetimesheet.ui.currenttimesheet.CurrentTimesheetScreen
import nz.co.bealetimesheet.ui.endshift.EndShiftScreen
import nz.co.bealetimesheet.ui.home.HomeScreen
import nz.co.bealetimesheet.ui.home.HomeViewModel
import nz.co.bealetimesheet.ui.home.HomeViewModelFactory
import nz.co.bealetimesheet.ui.restbreak.RestBreakScreen
import nz.co.bealetimesheet.ui.startshift.StartShiftScreen
import nz.co.bealetimesheet.ui.theme.BealeTimesheetTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

private enum class AppScreen {
    HOME,
    START_SHIFT,
    REST_BREAK,
    END_SHIFT,
    CURRENT_TIMESHEET
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BealeTimesheetTheme {
                val repository = remember {
                    val database = BealeDatabase.getDatabase(
                        applicationContext
                    )

                    TimesheetRepository(
                        database.timesheetDao()
                    )
                }

                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(repository)
                )

                val homeUiState by homeViewModel.uiState.collectAsState()

                val currentWeekStarting = remember {
                    LocalDate.now()
                        .with(
                            TemporalAdjusters.previousOrSame(
                                DayOfWeek.WEDNESDAY
                            )
                        )
                        .toString()
                }

                var currentScreen by rememberSaveable {
                    mutableStateOf(AppScreen.HOME)
                }

                when (currentScreen) {
                    AppScreen.HOME -> {
                        HomeScreen(
                            uiState = homeUiState,
                            onStartShift = {
                                homeViewModel.clearError()
                                currentScreen = AppScreen.START_SHIFT
                            },
                            onTakeRestBreak = {
                                homeViewModel.clearError()
                                currentScreen = AppScreen.REST_BREAK
                            },
                            onEndShift = {
                                homeViewModel.clearError()
                                currentScreen = AppScreen.END_SHIFT
                            },
                            onCurrentTimesheet = {
                                homeViewModel.clearError()
                                currentScreen =
                                    AppScreen.CURRENT_TIMESHEET
                            },
                            onExportAndEmail = {
// Export and Email will be connected later.
                            }
                        )
                    }

                    AppScreen.START_SHIFT -> {
                        StartShiftScreen(
                            isSaving = homeUiState.isLoading,
                            errorMessage = homeUiState.errorMessage,
                            onSave = {
                                    weekStarting,
                                    date,
                                    startTime ->

                                homeViewModel.startShift(
                                    weekStarting = weekStarting,
                                    date = date,
                                    startTime = startTime,
                                    onSuccess = {
                                        currentScreen = AppScreen.HOME
                                    }
                                )
                            },
                            onCancel = {
                                homeViewModel.clearError()
                                currentScreen = AppScreen.HOME
                            }
                        )
                    }

                    AppScreen.REST_BREAK -> {
                        RestBreakScreen(
                            isSaving = homeUiState.isLoading,
                            errorMessage = homeUiState.errorMessage,
                            onSave = {
                                    breakStartTime,
                                    breakFinishTime ->

                                homeViewModel.addRestBreak(
                                    breakStartTime = breakStartTime,
                                    breakFinishTime = breakFinishTime,
                                    onSuccess = {
                                        currentScreen = AppScreen.HOME
                                    }
                                )
                            },
                            onCancel = {
                                homeViewModel.clearError()
                                currentScreen = AppScreen.HOME
                            }
                        )
                    }

                    AppScreen.END_SHIFT -> {
                        EndShiftScreen(
                            isSaving = homeUiState.isLoading,
                            errorMessage = homeUiState.errorMessage,
                            onSave = {
                                    finishTime,
                                    comments ->

                                homeViewModel.finishShift(
                                    finishTime = finishTime,
                                    comments = comments,
                                    onSuccess = {
                                        currentScreen = AppScreen.HOME
                                    }
                                )
                            },
                            onCancel = {
                                homeViewModel.clearError()
                                currentScreen = AppScreen.HOME
                            }
                        )
                    }

                    AppScreen.CURRENT_TIMESHEET -> {
                        CurrentTimesheetScreen(
                            weekStarting = currentWeekStarting,
                            isLoading = false,
                            errorMessage = null,
                            onBack = {
                                currentScreen = AppScreen.HOME
                            }
                        )
                    }
                }
            }
        }
    }
}