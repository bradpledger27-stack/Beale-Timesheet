package nz.co.bealetimesheet.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shifts",
    foreignKeys = [
        ForeignKey(
            entity = TimesheetDay::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["dayId"]),
        Index(value = ["dayId", "shiftNumber"], unique = true)
    ]
)
data class Shift(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val dayId: Long,

    /**
     * Internal row number from 1 to 3.
     * This does not need to be shown to the user.
     */
    val shiftNumber: Int,

    /**
     * Stored as HH:mm.
     */
    val startTime: String,

    /**
     * Null while the shift is active.
     * Stored as HH:mm when completed.
     */
    val finishTime: String? = null,

    val updatedAt: Long = System.currentTimeMillis()
)