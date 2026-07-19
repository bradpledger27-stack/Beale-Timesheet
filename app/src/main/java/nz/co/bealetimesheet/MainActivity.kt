package nz.co.bealetimesheet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import nz.co.bealetimesheet.ui.home.HomeScreen
import nz.co.bealetimesheet.ui.theme.BealeTimesheetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BealeTimesheetTheme {
                HomeScreen()
            }
        }
    }
}