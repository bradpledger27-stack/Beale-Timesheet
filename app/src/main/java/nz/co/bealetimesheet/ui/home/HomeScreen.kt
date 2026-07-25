package nz.co.bealetimesheet.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onStartShift: () -> Unit,
    onTakeRestBreak: () -> Unit,
    onEndShift: () -> Unit,
    onCurrentTimesheet: () -> Unit,
    onSignature: () -> Unit,
    onExportAndEmail: () -> Unit,
    onSettings: () -> Unit
) {

    val displayFormatter = DateTimeFormatter.ofPattern("h:mm a")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "R&L Beale Log Transport LTD",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Timesheet",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        if (uiState.isLoading) {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text("Loading...")
                }
            }

        } else {

            ShiftCard(
                uiState = uiState,
                displayFormatter = displayFormatter,
                onStartShift = onStartShift,
                onTakeRestBreak = onTakeRestBreak,
                onEndShift = onEndShift
            )
        }

        uiState.errorMessage?.let {

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    "Current Pay Week",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    "Open and review your current Wednesday–Tuesday timesheet."
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Button(
                    onClick = onCurrentTimesheet,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Current Timesheet")
                }
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedButton(
                    onClick = onExportAndEmail,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export & Email")
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                OutlinedButton(
                    onClick = onSignature,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Employee Signature")
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                OutlinedButton(
                    onClick = onSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Settings")
                }
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        HorizontalDivider()

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text = "All changes are saved automatically.",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )
    }
}

@Composable
private fun ShiftCard(
    uiState: HomeUiState,
    displayFormatter: DateTimeFormatter,
    onStartShift: () -> Unit,
    onTakeRestBreak: () -> Unit,
    onEndShift: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "Current Shift",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            val activeShift = uiState.activeShift

            if (activeShift == null) {

                Text(
                    "Status: Not Active"
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Button(
                    onClick = onStartShift,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Shift")
                }

            } else {

                val startTime = runCatching {
                    LocalTime.parse(activeShift.startTime)
                        .format(displayFormatter)
                }.getOrDefault(activeShift.startTime)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Status")
                    Text(
                        "Active",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Started")
                    Text(startTime)
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Button(
                    onClick = onTakeRestBreak,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Take Rest Break")
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Button(
                    onClick = onEndShift,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("End Shift")
                }
            }
        }
    }
}