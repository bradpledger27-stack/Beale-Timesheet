package nz.co.bealetimesheet.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen() {

    var employee by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
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

        Text("Today's Entry")

        OutlinedTextField(
            value = employee,
            onValueChange = { employee = it },
            label = { Text("Employee Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Date: $today")

        OutlinedTextField(
            value = startTime,
            onValueChange = { startTime = it },
            label = { Text("Start Time (HH:mm)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Now")
        }

        OutlinedTextField(
            value = finishTime,
            onValueChange = { finishTime = it },
            label = { Text("Finish Time (HH:mm)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Now")
        }

        OutlinedTextField(
            value = comments,
            onValueChange = { comments = it },
            label = { Text("Comments") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}