package nz.co.bealetimesheet.data.repository

import kotlinx.coroutines.flow.Flow
import nz.co.bealetimesheet.data.dao.TimesheetDao
import nz.co.bealetimesheet.data.model.TimesheetEntry

class TimesheetRepository(
    private val timesheetDao: TimesheetDao
) {

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
}