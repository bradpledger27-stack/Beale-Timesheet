package nz.co.bealetimesheet.data.repository

import kotlinx.coroutines.flow.Flow
import nz.co.bealetimesheet.data.dao.TimesheetDao
import nz.co.bealetimesheet.data.model.RestBreak
import nz.co.bealetimesheet.data.model.Shift
import nz.co.bealetimesheet.data.model.TimesheetDay
import nz.co.bealetimesheet.data.model.TimesheetDayWithShifts
import nz.co.bealetimesheet.data.model.TimesheetEntry

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
        val day = createOrGetDay(
            employeeName = employeeName,
            weekStarting = weekStarting,
            date = date
        )

        val existingShiftCount = timesheetDao.getShiftCount(day.id)

        require(existingShiftCount < 3) {
            "A maximum of three shifts can be recorded for one day."
        }

        val activeShift = timesheetDao.getActiveShift()

        require(activeShift == null) {
            "A shift is already active."
        }

        val shift = Shift(
            dayId = day.id,
            shiftNumber = existingShiftCount + 1,
            startTime = startTime
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
        timesheetDao.deleteShift(shift)
    }

    /*
    * REST-BREAK FUNCTIONS
    */

    suspend fun addRestBreak(
        shiftId: Long,
        startTime: String,
        finishTime: String
    ): RestBreak {
        val restBreak = RestBreak(
            shiftId = shiftId,
            startTime = startTime,
            finishTime = finishTime
        )

        val restBreakId = timesheetDao.insertRestBreak(restBreak)

        return restBreak.copy(id = restBreakId)
    }

    fun observeRestBreaks(
        shiftId: Long
    ): Flow<List<RestBreak>> {
        return timesheetDao.observeRestBreaks(shiftId)
    }

    suspend fun updateRestBreak(restBreak: RestBreak) {
        timesheetDao.updateRestBreak(
            restBreak.copy(
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteRestBreak(restBreak: RestBreak) {
        timesheetDao.deleteRestBreak(restBreak)
    }
}