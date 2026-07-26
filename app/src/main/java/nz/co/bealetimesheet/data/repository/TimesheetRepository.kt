package nz.co.bealetimesheet.data.repository

import kotlinx.coroutines.flow.Flow
import nz.co.bealetimesheet.data.dao.TimesheetDao
import nz.co.bealetimesheet.data.model.RestBreak
import nz.co.bealetimesheet.data.model.Shift
import nz.co.bealetimesheet.data.model.TimesheetDay
import nz.co.bealetimesheet.data.model.TimesheetDayWithShifts
import nz.co.bealetimesheet.data.model.TimesheetEntry
import nz.co.bealetimesheet.data.model.TimesheetWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class TimesheetRepository(
    private val timesheetDao: TimesheetDao
) {

    /*
    * TEMPORARY OLD FUNCTIONS
    *
    * These keep the current Today screen working until it is replaced.
    */

    fun getAllEntries(): Flow<List<TimesheetEntry>> {
        return timesheetDao.getAllEntries()
    }

    suspend fun getEntryByDate(date: String): TimesheetEntry? {
        return timesheetDao.getEntryByDate(date)
    }

    suspend fun saveEntry(entry: TimesheetEntry) {
        val existingEntry = timesheetDao.getEntryByDate(entry.date)

        if (existingEntry == null) {
            timesheetDao.insert(entry)
        } else {
            timesheetDao.update(
                entry.copy(id = existingEntry.id)
            )
        }
    }

    suspend fun deleteEntry(entry: TimesheetEntry) {
        timesheetDao.delete(entry)
    }

    suspend fun deleteAllEntries() {
        timesheetDao.deleteAll()
    }

    /*
    * NEW DAILY TIMESHEET FUNCTIONS
    */

    suspend fun getDayByDate(date: String): TimesheetDay? {
        return timesheetDao.getDayByDate(date)
    }

    fun observeDayWithShifts(
        date: String
    ): Flow<TimesheetDayWithShifts?> {
        return timesheetDao.observeDayWithShifts(date)
    }

    fun observeWeek(
        weekStarting: String,
        weekEnding: String
    ): Flow<List<TimesheetDayWithShifts>> {
        return timesheetDao.observeWeek(
            weekStarting = weekStarting,
            weekEnding = weekEnding
        )
    }

    suspend fun createOrGetDay(
        employeeName: String,
        weekStarting: String,
        date: String
    ): TimesheetDay {
        requireWeekEditable(weekStarting)
        ensureWeekRecord(weekStarting)

        val existingDay = timesheetDao.getDayByDate(date)

        if (existingDay != null) {
            return existingDay
        }

        val day = TimesheetDay(
            employeeName = employeeName,
            weekStarting = weekStarting,
            date = date
        )

        val dayId = timesheetDao.insertDay(day)

        return day.copy(id = dayId)
    }

    suspend fun updateDayComments(
        dayId: Long,
        comments: String
    ) {
        requireDayEditable(dayId)
        timesheetDao.updateDayComments(
            dayId = dayId,
            comments = comments
        )
    }

    /*
    * SHIFT FUNCTIONS
    */

    suspend fun startShift(
        employeeName: String,
        weekStarting: String,
        date: String,
        startTime: String
    ): Shift {
        return addShift(
            employeeName = employeeName,
            weekStarting = weekStarting,
            date = date,
            startTime = startTime,
            finishTime = null
        )
    }

    fun observeRecordedWeekStarts(): Flow<List<String>> {
        return timesheetDao.observeRecordedWeekStarts()
    }

    fun observeWeekRecord(
        weekStarting: String
    ): Flow<TimesheetWeek?> {
        return timesheetDao.observeWeekRecord(weekStarting)
    }

    fun observeAllWeekRecords(): Flow<List<TimesheetWeek>> {
        return timesheetDao.observeAllWeeks()
    }

    suspend fun markWeekSubmitted(weekStarting: String) {
        ensureWeekRecord(weekStarting)
        timesheetDao.markWeekEmailed(weekStarting)
    }

    suspend fun unlockWeek(weekStarting: String) {
        ensureWeekRecord(weekStarting)
        timesheetDao.setWeekLocked(weekStarting, false)
    }

    suspend fun addShift(
        employeeName: String,
        weekStarting: String,
        date: String,
        startTime: String,
        finishTime: String?
    ): Shift {
        requireValidTime(startTime, "shift start")
        finishTime?.let { requireValidTime(it, "shift finish") }

        val day = createOrGetDay(
            employeeName = employeeName,
            weekStarting = weekStarting,
            date = date
        )

        val existingShifts = timesheetDao.getShiftsForDay(day.id)

        require(existingShifts.size < 3) {
            "A maximum of three shifts can be recorded for one day."
        }

        if (finishTime == null) {
            val activeShift = timesheetDao.getActiveShift()
            require(activeShift == null) {
                "A shift is already active."
            }
        }

        val nextShiftNumber = (1..3).first { candidate ->
            existingShifts.none { it.shiftNumber == candidate }
        }

        val shift = Shift(
            dayId = day.id,
            shiftNumber = nextShiftNumber,
            startTime = startTime,
            finishTime = finishTime
        )

        val shiftId = timesheetDao.insertShift(shift)

        return shift.copy(id = shiftId)
    }

    suspend fun getActiveShift(): Shift? {
        return timesheetDao.getActiveShift()
    }

    suspend fun finishShift(
        shiftId: Long,
        finishTime: String,
        comments: String
    ) {
        val shift = timesheetDao.getShiftById(shiftId)
            ?: error("The active shift could not be found.")

        requireDayEditable(shift.dayId)

        require(timesheetDao.getActiveRestBreak(shiftId) == null) {
            "Finish the active rest break before ending the shift."
        }

        timesheetDao.finishShift(
            shiftId = shiftId,
            finishTime = finishTime
        )

        timesheetDao.updateDayComments(
            dayId = shift.dayId,
            comments = comments
        )
    }

    suspend fun deleteShift(shift: Shift) {
        requireDayEditable(shift.dayId)
        timesheetDao.deleteShift(shift)
    }

    suspend fun updateShiftTimes(
        shift: Shift,
        startTime: String,
        finishTime: String?
    ) {
        requireDayEditable(shift.dayId)
        requireValidTime(startTime, "shift start")
        finishTime?.let { requireValidTime(it, "shift finish") }

        if (finishTime == null) {
            val otherActiveShift = timesheetDao.getActiveShift()
            require(
                otherActiveShift == null ||
                    otherActiveShift.id == shift.id
            ) {
                "Another shift is already active."
            }
        } else {
            require(timesheetDao.getActiveRestBreak(shift.id) == null) {
                "Finish the active rest break before completing the shift."
            }
        }

        timesheetDao.updateShift(
            shift.copy(
                startTime = startTime,
                finishTime = finishTime,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /*
    * REST-BREAK FUNCTIONS
    */

    suspend fun startRestBreak(
        shiftId: Long,
        startTime: String
    ): RestBreak {
        val shift = timesheetDao.getShiftById(shiftId)
            ?: error("The active shift could not be found.")

        requireDayEditable(shift.dayId)

        require(shift.finishTime == null) {
            "A rest break can only be started during an active shift."
        }

        require(timesheetDao.getActiveRestBreak(shiftId) == null) {
            "A rest break is already active."
        }

        val restBreak = RestBreak(
            shiftId = shiftId,
            startTime = startTime
        )

        val restBreakId = timesheetDao.insertRestBreak(restBreak)

        return restBreak.copy(id = restBreakId)
    }

    suspend fun getActiveRestBreak(
        shiftId: Long
    ): RestBreak? {
        return timesheetDao.getActiveRestBreak(shiftId)
    }

    suspend fun finishRestBreak(
        shiftId: Long,
        finishTime: String
    ): RestBreak {
        val activeRestBreak = timesheetDao.getActiveRestBreak(shiftId)
            ?: error("There is no active rest break.")

        val shift = timesheetDao.getShiftById(shiftId)
            ?: error("The active shift could not be found.")
        requireDayEditable(shift.dayId)

        val updatedRows = timesheetDao.finishRestBreak(
            restBreakId = activeRestBreak.id,
            finishTime = finishTime
        )

        check(updatedRows == 1) {
            "The active rest break could not be finished."
        }

        return activeRestBreak.copy(
            finishTime = finishTime,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun addRestBreak(
        shiftId: Long,
        startTime: String,
        finishTime: String
    ): RestBreak {
        val restBreak = startRestBreak(
            shiftId = shiftId,
            startTime = startTime
        )

        return finishRestBreak(
            shiftId = restBreak.shiftId,
            finishTime = finishTime
        )
    }

    fun observeRestBreaks(
        shiftId: Long
    ): Flow<List<RestBreak>> {
        return timesheetDao.observeRestBreaks(shiftId)
    }

    suspend fun updateRestBreak(restBreak: RestBreak) {
        val shift = timesheetDao.getShiftById(restBreak.shiftId)
            ?: error("The shift could not be found.")
        requireDayEditable(shift.dayId)

        requireValidTime(restBreak.startTime, "break start")
        restBreak.finishTime?.let {
            requireValidTime(it, "break finish")
        }

        if (restBreak.finishTime == null) {
            val otherActiveBreak = timesheetDao.getActiveRestBreak(
                restBreak.shiftId
            )
            require(
                otherActiveBreak == null ||
                    otherActiveBreak.id == restBreak.id
            ) {
                "Another rest break is already active."
            }
        }

        timesheetDao.updateRestBreak(
            restBreak.copy(
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteRestBreak(restBreak: RestBreak) {
        val shift = timesheetDao.getShiftById(restBreak.shiftId)
            ?: error("The shift could not be found.")
        requireDayEditable(shift.dayId)
        timesheetDao.deleteRestBreak(restBreak)
    }

    private suspend fun ensureWeekRecord(weekStarting: String) {
        if (timesheetDao.getWeek(weekStarting) == null) {
            timesheetDao.insertWeek(
                TimesheetWeek(weekStarting = weekStarting)
            )
        }
    }

    private suspend fun requireWeekEditable(weekStarting: String) {
        require(timesheetDao.getWeek(weekStarting)?.isLocked != true) {
            "This pay week is submitted and locked."
        }
    }

    private suspend fun requireDayEditable(dayId: Long) {
        val day = timesheetDao.getDayById(dayId)
            ?: error("The timesheet day could not be found.")
        requireWeekEditable(day.weekStarting)
        ensureWeekRecord(day.weekStarting)
        timesheetDao.touchWeek(day.weekStarting)
    }

    private fun requireValidTime(
        value: String,
        fieldName: String
    ) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        try {
            LocalTime.parse(value, formatter)
        } catch (_: DateTimeParseException) {
            throw IllegalArgumentException(
                "Enter $fieldName time as HH:mm, for example 06:30."
            )
        }
    }
}
