package nz.co.bealetimesheet.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import nz.co.bealetimesheet.data.model.TimesheetEntry

@Dao
interface TimesheetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TimesheetEntry)

    @Update
    suspend fun update(entry: TimesheetEntry)

    @Delete
    suspend fun delete(entry: TimesheetEntry)

    @Query("SELECT * FROM timesheet_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<TimesheetEntry>>

    @Query("SELECT * FROM timesheet_entries WHERE date = :date LIMIT 1")
    suspend fun getEntryByDate(date: String): TimesheetEntry?

    @Query("DELETE FROM timesheet_entries")
    suspend fun deleteAll()
}