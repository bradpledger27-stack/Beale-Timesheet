package nz.co.bealetimesheet.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen() {

    var employee by remember { mutableStateOf("Brad Pledger") }
    var startTime by remember { mutableStateOf("") }
    var breakLength by remember { mutableStateOf("30") }
    var finishTime by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf("") }

    val today = SimpleDateFormat(
        "EEEE dd MMM yyyy",
        Locale.getDefault()
    ).format(Date())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Today's Entry",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = employee,
            onValueChange = { employee = it },
            label = { Text("Employee Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Date: $today",
            style = MaterialTheme.typography.bodyLarge
        )

        Row {

            OutlinedTextField(
                value = startTime,
                onValueChange = { startTime = it },
                label = { Text("Start Time") },
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    startTime = LocalTime.now()
                        .format(DateTimeFormatter.ofPattern("HH:mm"))
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Now")
            }
        }

        OutlinedTextField(
            value = breakLength,
            onValueChange = { breakLength = it },
            label = { Text("Break Length (minutes)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row {

            OutlinedTextField(
                value = finishTime,
                onValueChange = { finishTime = it },
                label = { Text("Finish Time") },
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    finishTime = LocalTime.now()
                        .format(DateTimeFormatter.ofPattern("HH:mm"))
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Now")
            }
        }

        OutlinedTextField(
            value = comments,
            onValueChange = { comments = it },
            label = { Text("Comments") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Entry")
        }
    }
}