package nz.co.bealetimesheet.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onStartShift: () -> Unit,
    onTakeRestBreak: () -> Unit,
    onEndShift: () -> Unit,
    onCurrentTimesheet: () -> Unit,
    onExportAndEmail: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Beale Timesheet",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()

            Spacer(modifier = Modifier.height(24.dp))
        } else {
            val activeShift = uiState.activeShift

            if (activeShift == null) {
                Text(
                    text = "No Active Shift",
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = onStartShift,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ) {
                    Text("Start Shift")
                }
            } else {
                Text(
                    text = "Shift Active",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Started: ${activeShift.startTime}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Button(
                    onClick = onTakeRestBreak,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ) {
                    Text("Take Rest Break")
                }

                Button(
                    onClick = onEndShift,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text("End Shift")
                }
            }
        }

        uiState.errorMessage?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedButton(
            onClick = onCurrentTimesheet,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Current Timesheet")
        }

        OutlinedButton(
            onClick = onExportAndEmail,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text("Export & Email")
        }
    }
}