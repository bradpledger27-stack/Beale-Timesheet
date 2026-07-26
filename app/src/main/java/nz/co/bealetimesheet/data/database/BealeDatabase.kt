package nz.co.bealetimesheet.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 4,
    exportSchema = false
)
abstract class BealeDatabase : RoomDatabase() {

    abstract fun timesheetDao(): TimesheetDao

    companion object {

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS rest_breaks_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        shiftId INTEGER NOT NULL,
                        startTime TEXT NOT NULL,
                        finishTime TEXT,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY(shiftId) REFERENCES shifts(id)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    INSERT INTO rest_breaks_new (
                        id,
                        shiftId,
                        startTime,
                        finishTime,
                        updatedAt
                    )
                    SELECT
                        id,
                        shiftId,
                        startTime,
                        finishTime,
                        updatedAt
                    FROM rest_breaks
                    """.trimIndent()
                )

                database.execSQL("DROP TABLE rest_breaks")
                database.execSQL(
                    "ALTER TABLE rest_breaks_new RENAME TO rest_breaks"
                )
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_rest_breaks_shiftId
                    ON rest_breaks (shiftId)
                    """.trimIndent()
                )
            }
        }

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
                    .addMigrations(MIGRATION_3_4)
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
