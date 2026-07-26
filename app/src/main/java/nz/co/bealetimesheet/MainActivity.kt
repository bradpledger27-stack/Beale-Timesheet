package nz.co.bealetimesheet

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import nz.co.bealetimesheet.data.database.BealeDatabase
import nz.co.bealetimesheet.data.repository.TimesheetRepository
import nz.co.bealetimesheet.export.TimesheetPdfExporter
import nz.co.bealetimesheet.ui.currenttimesheet.CurrentTimesheetScreen
import nz.co.bealetimesheet.ui.currenttimesheet.CurrentTimesheetViewModel
import nz.co.bealetimesheet.ui.currenttimesheet.CurrentTimesheetViewModelFactory
import nz.co.bealetimesheet.ui.endshift.EndShiftScreen
import nz.co.bealetimesheet.ui.export.ExportScreen
import nz.co.bealetimesheet.ui.export.PdfPreviewScreen
import nz.co.bealetimesheet.ui.home.HomeScreen
import nz.co.bealetimesheet.ui.settings.SettingsRepository
import nz.co.bealetimesheet.ui.settings.SettingsScreen
import nz.co.bealetimesheet.ui.home.HomeViewModel
import nz.co.bealetimesheet.ui.home.HomeViewModelFactory
import nz.co.bealetimesheet.ui.history.TimesheetHistoryScreen
import nz.co.bealetimesheet.ui.backup.BackupRestoreScreen
import nz.co.bealetimesheet.backup.TimesheetBackupManager
import nz.co.bealetimesheet.ui.startshift.StartShiftScreen
import nz.co.bealetimesheet.ui.theme.BealeTimesheetTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.io.File
import android.graphics.Bitmap
import nz.co.bealetimesheet.ui.signature.SignatureRepository
import nz.co.bealetimesheet.ui.signature.SignatureScreen
import kotlinx.coroutines.launch

private enum class AppScreen {
    HOME,
    START_SHIFT,
    END_SHIFT,
    CURRENT_TIMESHEET,
    HISTORY,
    HISTORY_WEEK,
    SIGNATURE,
    PDF_PREVIEW,
    EXPORT,
    BACKUP_RESTORE,
    SETTINGS
    }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            BealeTimesheetTheme {
                val database = remember {
                    BealeDatabase.getDatabase(
                        applicationContext
                    )
                }

                val repository = remember(database) {
                    TimesheetRepository(
                        database.timesheetDao()
                    )
                }

                val backupManager = remember(database) {
                    TimesheetBackupManager(
                        context = applicationContext,
                        database = database
                    )
                }

                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(
                        repository = repository,
                        employeeNameProvider = {
                            SettingsRepository.getEmployeeName(
                                applicationContext
                            )
                        }
                    )
                )

                val homeUiState by
                homeViewModel.uiState.collectAsState()

                val currentWeekStarting = remember {
                    LocalDate.now()
                        .with(
                            TemporalAdjusters.previousOrSame(
                                DayOfWeek.WEDNESDAY
                            )
                        )
                        .toString()
                }

                val currentWeekEnding = remember(
                    currentWeekStarting
                ) {
                    LocalDate.parse(
                        currentWeekStarting
                    )
                        .plusDays(6)
                        .toString()
                }

                val currentTimesheetViewModel:
                        CurrentTimesheetViewModel = viewModel(
                    factory = CurrentTimesheetViewModelFactory(
                        repository = repository,
                        weekStarting = currentWeekStarting,
                        weekEnding = currentWeekEnding,
                        employeeNameProvider = {
                            SettingsRepository.getEmployeeName(
                                applicationContext
                            )
                        }
                    )
                )

                val currentTimesheetUiState by
                currentTimesheetViewModel.uiState.collectAsState()

                val recordedWeekStartsFlow = remember(repository) {
                    repository.observeRecordedWeekStarts()
                }
                val recordedWeekStarts by
                    recordedWeekStartsFlow.collectAsState(
                        initial = emptyList()
                    )

                val weekRecordsFlow = remember(repository) {
                    repository.observeAllWeekRecords()
                }
                val weekRecords by weekRecordsFlow.collectAsState(
                    initial = emptyList()
                )

                var currentScreen by rememberSaveable {
                    mutableStateOf(
                        AppScreen.HOME
                    )
                }

                var selectedHistoryWeek by rememberSaveable {
                    mutableStateOf<String?>(null)
                }

                var showSubmitConfirmation by rememberSaveable {
                    mutableStateOf(false)
                }

                var hasPreviewedCurrentWeek by rememberSaveable {
                    mutableStateOf(false)
                }

                var previewPdfPath by rememberSaveable {
                    mutableStateOf<String?>(null)
                }


                val coroutineScope = rememberCoroutineScope()

                val emailPreferences = remember {
                    getSharedPreferences(
                        "beale_timesheet_preferences",
                        MODE_PRIVATE
                    )
                }

                var recipientEmail by rememberSaveable {
                    mutableStateOf(
                        emailPreferences.getString(
                            "last_recipient_email",
                            "anna.bealeloggers@gmail.com"
                        ) ?: "anna.bealeloggers@gmail.com"
                    )
                }

                var tuesdayReminderEnabled by rememberSaveable {
                    mutableStateOf(
                        SettingsRepository.getTuesdayReminderEnabled(
                            applicationContext
                        )
                    )
                }

                var activeShiftReminderEnabled by rememberSaveable {
                    mutableStateOf(
                        SettingsRepository
                            .getActiveShiftReminderEnabled(
                                applicationContext
                            )
                    )
                }

                var use24HourTime by rememberSaveable {
                    mutableStateOf(
                        SettingsRepository.getUse24HourTime(
                            applicationContext
                        )
                    )
                }

                when (currentScreen) {
                    AppScreen.HOME -> {
                        HomeScreen(
                            uiState = homeUiState,
                            tuesdayReminderEnabled =
                                tuesdayReminderEnabled,
                            activeShiftReminderEnabled =
                                activeShiftReminderEnabled,
                            use24HourTime = use24HourTime,
                            onStartShift = {
                                homeViewModel.clearError()
                                currentScreen =
                                    AppScreen.START_SHIFT
                            },
                            onStartRestBreak = {
                                homeViewModel.clearError()
                                homeViewModel.startRestBreak(
                                    startTime = LocalTime.now()
                                        .withSecond(0)
                                        .withNano(0)
                                        .format(
                                            DateTimeFormatter.ofPattern(
                                                "HH:mm"
                                            )
                                        )
                                )
                            },
                            onFinishRestBreak = {
                                homeViewModel.clearError()
                                homeViewModel.finishRestBreak(
                                    finishTime = LocalTime.now()
                                        .withSecond(0)
                                        .withNano(0)
                                        .format(
                                            DateTimeFormatter.ofPattern(
                                                "HH:mm"
                                            )
                                        )
                                )
                            },
                            onEndShift = {
                                homeViewModel.clearError()
                                currentScreen =
                                    AppScreen.END_SHIFT
                            },
                            onCurrentTimesheet = {
                                homeViewModel.clearError()
                                currentScreen =
                                    AppScreen.CURRENT_TIMESHEET
                            },
                            onTimesheetHistory = {
                                homeViewModel.clearError()
                                currentScreen = AppScreen.HISTORY
                            },

                            onExportAndEmail = {
                                homeViewModel.clearError()

                                val previewFile =
                                    TimesheetPdfExporter
                                        .createBlankTemplatePdf(
                                            context = applicationContext,
                                            employeeName =
                                                SettingsRepository
                                                    .getEmployeeName(
                                                        applicationContext
                                                    ),
                                            weekStarting =
                                                currentWeekStarting,
                                            days =
                                                currentTimesheetUiState.days,
                                            includeSignature = false
                                        )

                                previewPdfPath = previewFile.absolutePath
                                hasPreviewedCurrentWeek = true
                                currentScreen = AppScreen.PDF_PREVIEW
                            },
                            onBackupRestore = {
                                currentScreen = AppScreen.BACKUP_RESTORE
                            },
                            onSettings = {
                                homeViewModel.clearError()
                                currentScreen = AppScreen.SETTINGS
                            }
                        )
                    }

                    AppScreen.START_SHIFT -> {
                        StartShiftScreen(
                            isSaving = homeUiState.isLoading,
                            errorMessage =
                                homeUiState.errorMessage,
                            use24HourTime = use24HourTime,
                            onSave = {
                                    weekStarting,
                                    date,
                                    startTime ->

                                homeViewModel.startShift(
                                    weekStarting =
                                        weekStarting,
                                    date = date,
                                    startTime = startTime,
                                    onSuccess = {
                                        currentScreen =
                                            AppScreen.HOME
                                    }
                                )
                            },
                            onCancel = {
                                homeViewModel.clearError()
                                currentScreen =
                                    AppScreen.HOME
                            }
                        )
                    }

                    AppScreen.END_SHIFT -> {
                        EndShiftScreen(
                            isSaving = homeUiState.isLoading,
                            errorMessage =
                                homeUiState.errorMessage,
                            use24HourTime = use24HourTime,
                            onSave = {
                                    finishTime,
                                    comments ->

                                homeViewModel.finishShift(
                                    finishTime =
                                        finishTime,
                                    comments = comments,
                                    onSuccess = {
                                        currentScreen =
                                            AppScreen.HOME
                                    }
                                )
                            },
                            onCancel = {
                                homeViewModel.clearError()
                                currentScreen =
                                    AppScreen.HOME
                            }
                        )
                    }

                    AppScreen.CURRENT_TIMESHEET -> {
                        CurrentTimesheetScreen(
                            weekStarting =
                                currentWeekStarting,
                            days =
                                currentTimesheetUiState.days,
                            isLoading =
                                currentTimesheetUiState.isLoading,
                            errorMessage =
                                currentTimesheetUiState.errorMessage,
                            isEditable =
                                !currentTimesheetUiState.isLocked,
                            isSubmitted =
                                currentTimesheetUiState.isSubmitted,
                            onUnlock = if (currentTimesheetUiState.isLocked) {
                                {
                                    coroutineScope.launch {
                                        repository.reopenWeek(
                                            currentWeekStarting
                                        )
                                    }
                                }
                            } else {
                                null
                            },
                            onAddShift = {
                                    date,
                                    startTime,
                                    finishTime,
                                    onSuccess ->
                                currentTimesheetViewModel.addShift(
                                    date = date,
                                    startTime = startTime,
                                    finishTime = finishTime,
                                    onSuccess = onSuccess
                                )
                            },
                            onUpdateShift = {
                                    shift,
                                    startTime,
                                    finishTime,
                                    comments,
                                    onSuccess ->
                                currentTimesheetViewModel.updateShift(
                                    shift = shift,
                                    startTime = startTime,
                                    finishTime = finishTime,
                                    comments = comments,
                                    onSuccess = onSuccess
                                )
                            },
                            onDeleteShift = { shift, onSuccess ->
                                currentTimesheetViewModel.deleteShift(
                                    shift = shift,
                                    onSuccess = onSuccess
                                )
                            },
                            onAddRestBreak = {
                                    shift,
                                    startTime,
                                    finishTime,
                                    onSuccess ->
                                currentTimesheetViewModel.addRestBreak(
                                    shift = shift,
                                    startTime = startTime,
                                    finishTime = finishTime,
                                    onSuccess = onSuccess
                                )
                            },
                            onUpdateRestBreak = {
                                    restBreak,
                                    startTime,
                                    finishTime,
                                    onSuccess ->
                                currentTimesheetViewModel.updateRestBreak(
                                    restBreak = restBreak,
                                    startTime = startTime,
                                    finishTime = finishTime,
                                    onSuccess = onSuccess
                                )
                            },
                            onDeleteRestBreak = {
                                    restBreak,
                                    onSuccess ->
                                currentTimesheetViewModel.deleteRestBreak(
                                    restBreak = restBreak,
                                    onSuccess = onSuccess
                                )
                            },
                            onBack = {
                                homeViewModel.refreshActiveShift()
                                currentScreen =
                                    AppScreen.HOME
                            }
                        )
                    }

                    AppScreen.HISTORY -> {
                        TimesheetHistoryScreen(
                            weekStarts = recordedWeekStarts,
                            weekRecords = weekRecords,
                            currentWeekStarting = currentWeekStarting,
                            onOpenWeek = { weekStarting ->
                                selectedHistoryWeek = weekStarting
                                currentScreen = AppScreen.HISTORY_WEEK
                            },
                            onBack = {
                                currentScreen = AppScreen.HOME
                            }
                        )
                    }

                    AppScreen.HISTORY_WEEK -> {
                        val weekStarting = selectedHistoryWeek

                        if (weekStarting == null) {
                            currentScreen = AppScreen.HISTORY
                        } else {
                            val historicalViewModel:
                                    CurrentTimesheetViewModel = viewModel(
                                key = "history-$weekStarting",
                                factory =
                                    CurrentTimesheetViewModelFactory(
                                        repository = repository,
                                        weekStarting = weekStarting,
                                        weekEnding =
                                            LocalDate.parse(weekStarting)
                                                .plusDays(6)
                                                .toString(),
                                        employeeNameProvider = {
                                            SettingsRepository
                                                .getEmployeeName(
                                                    applicationContext
                                                )
                                        }
                                    )
                            )
                            val historicalState by
                                historicalViewModel.uiState.collectAsState()

                            CurrentTimesheetScreen(
                                weekStarting = weekStarting,
                                days = historicalState.days,
                                isLoading = historicalState.isLoading,
                                errorMessage = historicalState.errorMessage,
                                isEditable = !historicalState.isLocked,
                                isSubmitted =
                                    historicalState.isSubmitted,
                                onUnlock =
                                    if (historicalState.isLocked) {
                                        {
                                            coroutineScope.launch {
                                                repository.unlockWeek(
                                                    weekStarting
                                                )
                                            }
                                        }
                                    } else {
                                        null
                                    },
                                onExport = {
                                    val pdfFile =
                                        TimesheetPdfExporter
                                            .createBlankTemplatePdf(
                                                context =
                                                    applicationContext,
                                                employeeName =
                                                    historicalState.days
                                                        .firstOrNull()
                                                        ?.day
                                                        ?.employeeName
                                                        ?: SettingsRepository
                                                            .getEmployeeName(
                                                                applicationContext
                                                            ),
                                                weekStarting = weekStarting,
                                                days = historicalState.days
                                            )

                                    val pdfUri =
                                        FileProvider.getUriForFile(
                                            applicationContext,
                                            applicationContext.packageName +
                                                ".provider",
                                            pdfFile
                                        )

                                    val shareIntent =
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "application/pdf"
                                            putExtra(
                                                Intent.EXTRA_EMAIL,
                                                arrayOf(recipientEmail)
                                            )
                                            putExtra(
                                                Intent.EXTRA_STREAM,
                                                pdfUri
                                            )
                                            putExtra(
                                                Intent.EXTRA_SUBJECT,
                                                "R&L Beale Log Transport " +
                                                    "LTD Timesheet - " +
                                                    "Week Starting " +
                                                    weekStarting
                                            )
                                            addFlags(
                                                Intent
                                                    .FLAG_GRANT_READ_URI_PERMISSION
                                            )
                                        }

                                    startActivity(
                                        Intent.createChooser(
                                            shareIntent,
                                            "Email timesheet"
                                        )
                                    )
                                },
                                onAddShift = {
                                        date,
                                        startTime,
                                        finishTime,
                                        onSuccess ->
                                    historicalViewModel.addShift(
                                        date = date,
                                        startTime = startTime,
                                        finishTime = finishTime,
                                        onSuccess = onSuccess
                                    )
                                },
                                onUpdateShift = {
                                        shift,
                                        startTime,
                                        finishTime,
                                        comments,
                                        onSuccess ->
                                    historicalViewModel.updateShift(
                                        shift = shift,
                                        startTime = startTime,
                                        finishTime = finishTime,
                                        comments = comments,
                                        onSuccess = onSuccess
                                    )
                                },
                                onDeleteShift = { shift, onSuccess ->
                                    historicalViewModel.deleteShift(
                                        shift,
                                        onSuccess
                                    )
                                },
                                onAddRestBreak = {
                                    shift,
                                    startTime,
                                    finishTime,
                                    onSuccess ->
                                currentTimesheetViewModel.addRestBreak(
                                    shift = shift,
                                    startTime = startTime,
                                    finishTime = finishTime,
                                    onSuccess = onSuccess
                                )
                            },
                            onUpdateRestBreak = {
                                        restBreak,
                                        startTime,
                                        finishTime,
                                        onSuccess ->
                                    historicalViewModel.updateRestBreak(
                                        restBreak,
                                        startTime,
                                        finishTime,
                                        onSuccess
                                    )
                                },
                                onDeleteRestBreak = {
                                        restBreak,
                                        onSuccess ->
                                    historicalViewModel.deleteRestBreak(
                                        restBreak,
                                        onSuccess
                                    )
                                },
                                onBack = {
                                    currentScreen = AppScreen.HISTORY
                                }
                            )
                        }
                    }

                    AppScreen.PDF_PREVIEW -> {
                        val previewFile = previewPdfPath?.let(::File)

                        if (previewFile != null && previewFile.exists()) {
                            PdfPreviewScreen(
                                pdfFile = previewFile,
                                recipientEmail = recipientEmail,
                                onRecipientEmailChange = { newEmail ->
                                    recipientEmail = newEmail
                                },
                                onSignAndSubmit = {
                                    currentScreen = AppScreen.SIGNATURE
                                },
                                onBack = {
                                    currentScreen = AppScreen.HOME
                                }
                            )
                        } else {
                            Text("Unable to open the timesheet preview.")
                        }
                    }
                    AppScreen.SIGNATURE -> {
                        SignatureScreen(
                            onSave = { bitmap: Bitmap ->
                                SignatureRepository.saveSignature(
                                    applicationContext,
                                    bitmap
                                )

                                val emailAddress = recipientEmail.trim()

                                emailPreferences
                                    .edit()
                                    .putString(
                                        "last_recipient_email",
                                        emailAddress
                                    )
                                    .apply()

                                val pdfFile =
                                    TimesheetPdfExporter
                                        .createBlankTemplatePdf(
                                            context = applicationContext,
                                            employeeName =
                                                SettingsRepository
                                                    .getEmployeeName(
                                                        applicationContext
                                                    ),
                                            weekStarting =
                                                currentWeekStarting,
                                            days =
                                                currentTimesheetUiState.days,
                                            includeSignature = true
                                        )

                                val pdfUri =
                                    FileProvider.getUriForFile(
                                        applicationContext,
                                        applicationContext.packageName +
                                            ".provider",
                                        pdfFile
                                    )

                                val shareIntent =
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(
                                            Intent.EXTRA_EMAIL,
                                            arrayOf(emailAddress)
                                        )
                                        putExtra(
                                            Intent.EXTRA_STREAM,
                                            pdfUri
                                        )
                                        putExtra(
                                            Intent.EXTRA_SUBJECT,
                                            "R&L Beale Log Transport LTD " +
                                                "Timesheet - Week Starting " +
                                                currentWeekStarting
                                        )
                                        addFlags(
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        )
                                    }

                                currentScreen = AppScreen.EXPORT

                                startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        "Email timesheet"
                                    )
                                )

                                showSubmitConfirmation = true
                            },
                            onCancel = {
                                currentScreen = AppScreen.PDF_PREVIEW
                            }
                        )
                    }
                    AppScreen.EXPORT -> {
                        ExportScreen(
                            recipientEmail = recipientEmail,
                            onRecipientEmailChange = { newEmail ->
                                recipientEmail = newEmail
                            },
                            hasPreviewed = hasPreviewedCurrentWeek,
                            onPreviewPdf = {
                                val previewFile =
                                    TimesheetPdfExporter
                                        .createBlankTemplatePdf(
                                            context = applicationContext,
                                            employeeName =
                                                SettingsRepository
                                                    .getEmployeeName(
                                                        applicationContext
                                                    ),
                                            weekStarting =
                                                currentWeekStarting,
                                            days =
                                                currentTimesheetUiState.days,
                                            includeSignature = false
                                        )

                                val previewUri =
                                    FileProvider.getUriForFile(
                                        applicationContext,
                                        applicationContext.packageName +
                                            ".provider",
                                        previewFile
                                    )

                                val previewIntent =
                                    Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(
                                            previewUri,
                                            "application/pdf"
                                        )
                                        addFlags(
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        )
                                    }

                                hasPreviewedCurrentWeek = true

                                startActivity(
                                    Intent.createChooser(
                                        previewIntent,
                                        "Preview timesheet"
                                    )
                                )
                            },
                            onSignAndSubmit = {
                                currentScreen = AppScreen.SIGNATURE
                            },
                            onBack = {
                                currentScreen = AppScreen.HOME
                            }
                        )
                    }
                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            initialEmployeeName = SettingsRepository.getEmployeeName(
                                applicationContext
                            ),
                            initialRecipientEmail = recipientEmail,
                            initialTuesdayReminderEnabled =
                                tuesdayReminderEnabled,
                            initialActiveShiftReminderEnabled =
                                activeShiftReminderEnabled,
                            initialUse24HourTime =
                                use24HourTime,
                            onSave = {
                                    employeeName,
                                    email,
                                    tuesdayReminder,
                                    activeShiftReminder,
                                    use24Hour ->
                                SettingsRepository.saveEmployeeName(
                                    applicationContext,
                                    employeeName
                                )

                                SettingsRepository.saveRecipientEmail(
                                    applicationContext,
                                    email
                                )

                                SettingsRepository
                                    .saveTuesdayReminderEnabled(
                                        applicationContext,
                                        tuesdayReminder
                                    )

                                SettingsRepository
                                    .saveActiveShiftReminderEnabled(
                                        applicationContext,
                                        activeShiftReminder
                                    )

                                SettingsRepository.saveUse24HourTime(
                                    applicationContext,
                                    use24Hour
                                )

                                recipientEmail = email
                                tuesdayReminderEnabled = tuesdayReminder
                                activeShiftReminderEnabled =
                                    activeShiftReminder
                                use24HourTime = use24Hour
                                currentScreen = AppScreen.HOME
                            },
                            onCancel = {
                                currentScreen = AppScreen.HOME
                            }
                        )
                    }

                    AppScreen.BACKUP_RESTORE -> {
                        BackupRestoreScreen(
                            onExport = { uri ->
                                backupManager.exportBackup(uri)
                            },
                            onRestore = { uri ->
                                backupManager.importBackup(uri)
                            },
                            onRestored = {
                                recreate()
                            },
                            onBack = {
                                currentScreen = AppScreen.HOME
                            }
                        )
                    }
                }

                if (showSubmitConfirmation) {
                    AlertDialog(
                        onDismissRequest = {
                            showSubmitConfirmation = false
                        },
                        title = {
                            Text("Mark pay week as submitted?")
                        },
                        text = {
                            Text(
                                "Choose Mark Submitted only after you " +
                                    "have completed sending the email. " +
                                    "The week will be locked against " +
                                    "accidental changes."
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        repository.markWeekSubmitted(
                                            currentWeekStarting
                                        )
                                        showSubmitConfirmation = false
                                        currentScreen = AppScreen.HOME
                                    }
                                }
                            ) {
                                Text("Mark Submitted")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showSubmitConfirmation = false
                                }
                            ) {
                                Text("Not Yet")
                            }
                        }
                    )
                }
            }
        }
    }
}
