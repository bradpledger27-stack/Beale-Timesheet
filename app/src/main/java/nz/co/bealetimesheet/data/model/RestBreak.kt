package nz.co.bealetimesheet.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rest_breaks",
    foreignKeys = [
        ForeignKey(
            entity = Shift::class,
            parentColumns = ["id"],
            childColumns = ["shiftId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["shiftId"])
    ]
)
data class RestBreak(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val shiftId: Long,

    /**
     * Stored as HH:mm.
     */
    val startTime: String,

    /**
     * Stored as HH:mm.
     */
    val finishTime: String,

    val updatedAt: Long = System.currentTimeMillis()
)