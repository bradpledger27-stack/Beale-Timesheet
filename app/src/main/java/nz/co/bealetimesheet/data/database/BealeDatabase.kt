package nz.co.bealetimesheet.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import nz.co.bealetimesheet.data.dao.TimesheetDao
import nz.co.bealetimesheet.data.model.TimesheetEntry

@Database(
    entities = [TimesheetEntry::class],
    version = 1,
    exportSchema = false
)
abstract class BealeDatabase : RoomDatabase() {

    abstract fun timesheetDao(): TimesheetDao

    companion object {
        @Volatile
        private var INSTANCE: BealeDatabase? = null

        fun getDatabase(context: Context): BealeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BealeDatabase::class.java,
                    "beale_timesheet_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}