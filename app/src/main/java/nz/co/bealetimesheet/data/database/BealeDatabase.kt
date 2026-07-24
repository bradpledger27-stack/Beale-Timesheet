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

@Database(
    entities = [
        TimesheetEntry::class,
        TimesheetDay::class,
        Shift::class,
        RestBreak::class
    ],
    version = 2,
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
                )
                    /*
                    * This is suitable while the app is still being developed.
                    * It recreates the database when its structure changes.
                    */
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}