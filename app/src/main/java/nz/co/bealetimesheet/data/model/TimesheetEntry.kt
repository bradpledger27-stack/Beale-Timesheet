package nz.co.bealetimesheet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timesheet_entries")
data class TimesheetEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val employeeName: String,

    /**
     * Stored as yyyy-MM-dd, for example 2026-07-22.
     */
    val date: String,

    /**
     * Stored as HH:mm, for example 05:30.
     */
    val startTime: String,

    /**
     * Stored as HH:mm, for example 17:00.
     */
    val finishTime: String,

    /**
     * Optional 30-minute break start time.
     */
    val breakStartTime: String? = null,

    /**
     * Optional 30-minute break finish time.
     */
    val breakFinishTime: String? = null,

    val comments: String = "",

    /**
     * Time when the entry was last saved.
     */
    val updatedAt: Long = System.currentTimeMillis()
)