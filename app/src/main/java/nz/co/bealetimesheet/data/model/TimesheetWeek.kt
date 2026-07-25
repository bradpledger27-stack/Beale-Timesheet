package nz.co.bealetimesheet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One record for each Wednesday-to-Tuesday pay week.
 */
@Entity(
    tableName = "timesheet_weeks"
)
data class TimesheetWeek(

    /**
     * Wednesday that begins the pay week.
     * Stored as yyyy-MM-dd.
     */
    @PrimaryKey
    val weekStarting: String,

    /**
     * True after the timesheet has been emailed.
     */
    val isSubmitted: Boolean = false,

    /**
     * Submitted weeks are normally protected from accidental editing.
     */
    val isLocked: Boolean = false,

    /**
     * Time the week was most recently emailed.
     */
    val lastEmailedAt: Long? = null,

    /**
     * Updated whenever something in this week changes.
     *
     * Comparing this value with lastEmailedAt lets us detect whether
     * an emailed timesheet has since been edited.
     */
    val updatedAt: Long = System.currentTimeMillis()
)