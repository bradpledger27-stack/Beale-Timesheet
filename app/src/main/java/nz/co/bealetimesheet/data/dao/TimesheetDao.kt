package nz.co.bealetimesheet.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import nz.co.bealetimesheet.data.model.RestBreak
import nz.co.bealetimesheet.data.model.Shift
import nz.co.bealetimesheet.data.model.TimesheetDay
import nz.co.bealetimesheet.data.model.TimesheetDayWithShifts
import nz.co.bealetimesheet.data.model.TimesheetEntry
import nz.co.bealetimesheet.data.model.TimesheetWeek

@Dao
interface TimesheetDao {

    @Query("SELECT * FROM timesheet_entries ORDER BY id")
    suspend fun getAllEntriesOnce(): List<TimesheetEntry>

    @Query("SELECT * FROM timesheet_weeks ORDER BY weekStarting")
    suspend fun getAllWeeksOnce(): List<TimesheetWeek>

    @Query("SELECT * FROM timesheet_days ORDER BY id")
    suspend fun getAllDaysOnce(): List<TimesheetDay>

    @Query("SELECT * FROM shifts ORDER BY id")
    suspend fun getAllShiftsOnce(): List<Shift>

    @Query("SELECT * FROM rest_breaks ORDER BY id")
    suspend fun getAllRestBreaksOnce(): List<RestBreak>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreEntries(entries: List<TimesheetEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreWeeks(weeks: List<TimesheetWeek>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreDays(days: List<TimesheetDay>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreShifts(shifts: List<Shift>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreRestBreaks(restBreaks: List<RestBreak>)

    @Query("DELETE FROM rest_breaks")
    suspend fun clearRestBreaks()

    @Query("DELETE FROM shifts")
    suspend fun clearShifts()

    @Query("DELETE FROM timesheet_days")
    suspend fun clearDays()

    @Query("DELETE FROM timesheet_weeks")
    suspend fun clearWeeks()

    /*
     * TEMPORARY OLD METHODS
     *
     * These remain here so the existing screens continue compiling
     * while the newer timesheet workflow is developed.
     */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TimesheetEntry)

    @Update
    suspend fun update(entry: TimesheetEntry)

    @Delete
    suspend fun delete(entry: TimesheetEntry)

    @Query(
        """
        SELECT * FROM timesheet_entries
        ORDER BY date DESC
        """
    )
    fun getAllEntries(): Flow<List<TimesheetEntry>>

    @Query(
        """
        SELECT * FROM timesheet_entries
        WHERE date = :date
        LIMIT 1
        """
    )
    suspend fun getEntryByDate(date: String): TimesheetEntry?

    @Query("DELETE FROM timesheet_entries")
    suspend fun deleteAll()

    /*
     * PAY-WEEK METHODS
     */

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWeek(week: TimesheetWeek): Long

    @Update
    suspend fun updateWeek(week: TimesheetWeek)

    @Query(
        """
        SELECT * FROM timesheet_weeks
        WHERE weekStarting = :weekStarting
        LIMIT 1
        """
    )
    suspend fun getWeek(
        weekStarting: String
    ): TimesheetWeek?

    @Query(
        """
        SELECT * FROM timesheet_weeks
        WHERE weekStarting = :weekStarting
        LIMIT 1
        """
    )
    fun observeWeekRecord(
        weekStarting: String
    ): Flow<TimesheetWeek?>

    @Query(
        """
        SELECT * FROM timesheet_weeks
        ORDER BY weekStarting DESC
        """
    )
    fun observeAllWeeks(): Flow<List<TimesheetWeek>>

    @Query(
        """
        UPDATE timesheet_weeks
        SET isSubmitted = 1,
            isLocked = 1,
            lastEmailedAt = :emailedAt,
            updatedAt = :emailedAt
        WHERE weekStarting = :weekStarting
        """
    )
    suspend fun markWeekEmailed(
        weekStarting: String,
        emailedAt: Long = System.currentTimeMillis()
    )

    @Query(
        """
        UPDATE timesheet_weeks
        SET isLocked = :isLocked
        WHERE weekStarting = :weekStarting
        """
    )
    suspend fun setWeekLocked(
        weekStarting: String,
        isLocked: Boolean
    )

    @Query(
        """
    UPDATE timesheet_weeks
    SET isSubmitted = 0,
        isLocked = 0,
        lastEmailedAt = NULL,
        updatedAt = :updatedAt
    WHERE weekStarting = :weekStarting
    """
    )
    suspend fun reopenWeek(
        weekStarting: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query(
        """
        UPDATE timesheet_weeks
        SET updatedAt = :updatedAt
        WHERE weekStarting = :weekStarting
        """
    )
    suspend fun touchWeek(
        weekStarting: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    /*
     * DAILY TIMESHEET METHODS
     */

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDay(day: TimesheetDay): Long

    @Update
    suspend fun updateDay(day: TimesheetDay)

    @Query(
        """
        SELECT * FROM timesheet_days
        WHERE date = :date
        LIMIT 1
        """
    )
    suspend fun getDayByDate(date: String): TimesheetDay?

    @Query("SELECT * FROM timesheet_days WHERE id = :dayId LIMIT 1")
    suspend fun getDayById(dayId: Long): TimesheetDay?

    @Transaction
    @Query(
        """
        SELECT * FROM timesheet_days
        WHERE date = :date
        LIMIT 1
        """
    )
    fun observeDayWithShifts(
        date: String
    ): Flow<TimesheetDayWithShifts?>

    @Transaction
    @Query(
        """
        SELECT * FROM timesheet_days
        WHERE date BETWEEN :weekStarting AND :weekEnding
        ORDER BY date ASC
        """
    )
    fun observeWeek(
        weekStarting: String,
        weekEnding: String
    ): Flow<List<TimesheetDayWithShifts>>

    @Query(
        """
        SELECT DISTINCT weekStarting
        FROM timesheet_days
        ORDER BY weekStarting DESC
        """
    )
    fun observeRecordedWeekStarts(): Flow<List<String>>

    @Query(
        """
        UPDATE timesheet_days
        SET comments = :comments,
            updatedAt = :updatedAt
        WHERE id = :dayId
        """
    )
    suspend fun updateDayComments(
        dayId: Long,
        comments: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    /*
     * SHIFT METHODS
     */

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertShift(shift: Shift): Long

    @Update
    suspend fun updateShift(shift: Shift)

    @Delete
    suspend fun deleteShift(shift: Shift)

    @Query(
        """
        SELECT COUNT(*) FROM shifts
        WHERE dayId = :dayId
        """
    )
    suspend fun getShiftCount(dayId: Long): Int

    @Query(
        """
        SELECT * FROM shifts
        WHERE dayId = :dayId
        ORDER BY shiftNumber ASC
        """
    )
    suspend fun getShiftsForDay(dayId: Long): List<Shift>

    @Query(
        """
        SELECT * FROM shifts
        WHERE finishTime IS NULL
        ORDER BY id DESC
        LIMIT 1
        """
    )
    suspend fun getActiveShift(): Shift?

    @Query(
        """
        SELECT * FROM shifts
        WHERE id = :shiftId
        LIMIT 1
        """
    )
    suspend fun getShiftById(
        shiftId: Long
    ): Shift?

    @Query(
        """
        UPDATE shifts
        SET finishTime = :finishTime,
            updatedAt = :updatedAt
        WHERE id = :shiftId
        """
    )
    suspend fun finishShift(
        shiftId: Long,
        finishTime: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    /*
     * REST-BREAK METHODS
     */

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRestBreak(
        restBreak: RestBreak
    ): Long

    @Update
    suspend fun updateRestBreak(
        restBreak: RestBreak
    )

    @Delete
    suspend fun deleteRestBreak(
        restBreak: RestBreak
    )

    @Query(
        """
        SELECT * FROM rest_breaks
        WHERE shiftId = :shiftId
        ORDER BY startTime ASC
        """
    )
    fun observeRestBreaks(
        shiftId: Long
    ): Flow<List<RestBreak>>

    @Query(
        """
        SELECT * FROM rest_breaks
        WHERE shiftId = :shiftId
          AND finishTime IS NULL
        ORDER BY id DESC
        LIMIT 1
        """
    )
    suspend fun getActiveRestBreak(
        shiftId: Long
    ): RestBreak?

    @Query(
        """
        UPDATE rest_breaks
        SET finishTime = :finishTime,
            updatedAt = :updatedAt
        WHERE id = :restBreakId
          AND finishTime IS NULL
        """
    )
    suspend fun finishRestBreak(
        restBreakId: Long,
        finishTime: String,
        updatedAt: Long = System.currentTimeMillis()
    ): Int
}
