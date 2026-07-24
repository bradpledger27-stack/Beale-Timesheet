package nz.co.bealetimesheet.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "timesheet_days",
    indices = [
        Index(value = ["date"], unique = true),
        Index(value = ["weekStarting"])
    ]
)
data class TimesheetDay(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val employeeName: String,

    /**
     * Wednesday that begins the pay week.
     * Stored as yyyy-MM-dd.
     */
    val weekStarting: String,

    /**
     * The actual work date.
     * Stored as yyyy-MM-dd.
     */
    val date: String,

    /**
     * One comments section shared by all shifts for this day.
     */
    val comments: String = "",

    val updatedAt: Long = System.currentTimeMillis()
)