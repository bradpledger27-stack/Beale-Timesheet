package nz.co.bealetimesheet.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import nz.co.bealetimesheet.data.dao.TimesheetDao
import nz.co.bealetimesheet.data.model.RestBreak
import nz.co.bealetimesheet.data.model.Shift
import nz.co.bealetimesheet.data.model.TimesheetDay
import nz.co.bealetimesheet.data.model.TimesheetEntry
import nz.co.bealetimesheet.data.model.TimesheetWeek

@Database(
    entities = [
        TimesheetEntry::class,
        TimesheetWeek::class,
        TimesheetDay::class,
        Shift::class,
        RestBreak::class
    ],
    version = 3,
    exportSchema = false
)
abstract class BealeDatabase : RoomDatabase() {

    abstract fun timesheetDao(): TimesheetDao

    companion object {

        @Volatile
        private var INSTANCE: BealeDatabase? = null

        fun getDatabase(
            context: Context
        ): BealeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BealeDatabase::class.java,
                    "beale_timesheet_database"
                )
                    /*
                     * Suitable while the app is still being developed.
                     *
                     * Changing the database version currently erases and
                     * recreates the local database.
                     */
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}