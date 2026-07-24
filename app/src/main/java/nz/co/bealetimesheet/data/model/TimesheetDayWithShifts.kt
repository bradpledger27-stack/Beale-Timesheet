package nz.co.bealetimesheet.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class ShiftWithBreaks(
    @Embedded
    val shift: Shift,

    @Relation(
        parentColumn = "id",
        entityColumn = "shiftId"
    )
    val restBreaks: List<RestBreak>
)

data class TimesheetDayWithShifts(
    @Embedded
    val day: TimesheetDay,

    @Relation(
        entity = Shift::class,
        parentColumn = "id",
        entityColumn = "dayId"
    )
    val shifts: List<ShiftWithBreaks>
)