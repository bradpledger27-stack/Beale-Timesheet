package nz.co.bealetimesheet.backup

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nz.co.bealetimesheet.data.database.BealeDatabase
import nz.co.bealetimesheet.data.model.RestBreak
import nz.co.bealetimesheet.data.model.Shift
import nz.co.bealetimesheet.data.model.TimesheetDay
import nz.co.bealetimesheet.data.model.TimesheetEntry
import nz.co.bealetimesheet.data.model.TimesheetWeek
import nz.co.bealetimesheet.ui.settings.SettingsRepository
import nz.co.bealetimesheet.ui.signature.SignatureRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class TimesheetBackupManager(
    private val context: Context,
    private val database: BealeDatabase
) {
    suspend fun exportBackup(uri: Uri) = withContext(Dispatchers.IO) {
        val dao = database.timesheetDao()
        val root = JSONObject().apply {
            put("format", "beale-timesheet-backup")
            put("version", 1)
            put("createdAt", System.currentTimeMillis())
            put("entries", entriesToJson(dao.getAllEntriesOnce()))
            put("weeks", weeksToJson(dao.getAllWeeksOnce()))
            put("days", daysToJson(dao.getAllDaysOnce()))
            put("shifts", shiftsToJson(dao.getAllShiftsOnce()))
            put(
                "restBreaks",
                restBreaksToJson(dao.getAllRestBreaksOnce())
            )
            put("settings", settingsToJson())
            put("signaturePng", signatureToBase64())
        }

        requireNotNull(context.contentResolver.openOutputStream(uri)) {
            "Unable to open the selected backup file."
        }.bufferedWriter().use { writer ->
            writer.write(root.toString(2))
        }
    }

    suspend fun importBackup(uri: Uri) = withContext(Dispatchers.IO) {
        val jsonText = requireNotNull(
            context.contentResolver.openInputStream(uri)
        ) {
            "Unable to open the selected backup file."
        }.bufferedReader().use { it.readText() }

        val root = JSONObject(jsonText)
        require(root.optString("format") == "beale-timesheet-backup") {
            "This is not an R&L Beale Log Transport LTD backup file."
        }
        require(root.optInt("version") == 1) {
            "This backup version is not supported."
        }

        val entries = root.getJSONArray("entries").toEntries()
        val weeks = root.getJSONArray("weeks").toWeeks()
        val days = root.getJSONArray("days").toDays()
        val shifts = root.getJSONArray("shifts").toShifts()
        val restBreaks = root.getJSONArray("restBreaks").toRestBreaks()

        database.withTransaction {
            val dao = database.timesheetDao()
            dao.clearRestBreaks()
            dao.clearShifts()
            dao.clearDays()
            dao.clearWeeks()
            dao.deleteAll()

            if (entries.isNotEmpty()) dao.restoreEntries(entries)
            if (weeks.isNotEmpty()) dao.restoreWeeks(weeks)
            if (days.isNotEmpty()) dao.restoreDays(days)
            if (shifts.isNotEmpty()) dao.restoreShifts(shifts)
            if (restBreaks.isNotEmpty()) {
                dao.restoreRestBreaks(restBreaks)
            }
        }

        restoreSettings(root.getJSONObject("settings"))
        restoreSignature(root.optString("signaturePng"))
    }

    private fun settingsToJson() = JSONObject().apply {
        put(
            "employeeName",
            SettingsRepository.getEmployeeName(context)
        )
        put(
            "recipientEmail",
            SettingsRepository.getRecipientEmail(context)
        )
        put(
            "tuesdayReminderEnabled",
            SettingsRepository.getTuesdayReminderEnabled(context)
        )
        put(
            "activeShiftReminderEnabled",
            SettingsRepository.getActiveShiftReminderEnabled(context)
        )
    }

    private fun restoreSettings(settings: JSONObject) {
        SettingsRepository.saveEmployeeName(
            context,
            settings.getString("employeeName")
        )
        SettingsRepository.saveRecipientEmail(
            context,
            settings.getString("recipientEmail")
        )
        SettingsRepository.saveTuesdayReminderEnabled(
            context,
            settings.optBoolean("tuesdayReminderEnabled", true)
        )
        SettingsRepository.saveActiveShiftReminderEnabled(
            context,
            settings.optBoolean("activeShiftReminderEnabled", true)
        )
    }

    private fun signatureToBase64(): String {
        val bitmap = SignatureRepository.loadSignature(context)
            ?: return ""
        return ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            if (!bitmap.isRecycled) bitmap.recycle()
            Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        }
    }

    private fun restoreSignature(encoded: String) {
        if (encoded.isBlank()) {
            SignatureRepository.deleteSignature(context)
            return
        }
        val bytes = Base64.decode(encoded, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: error("The signature in the backup is invalid.")
        SignatureRepository.saveSignature(context, bitmap)
        if (!bitmap.isRecycled) bitmap.recycle()
    }
}

private fun entriesToJson(items: List<TimesheetEntry>) =
    JSONArray().apply {
    items.forEach { item ->
        put(JSONObject().apply {
            put("id", item.id)
            put("employeeName", item.employeeName)
            put("date", item.date)
            put("startTime", item.startTime)
            put("finishTime", item.finishTime)
            put("breakStartTime", item.breakStartTime)
            put("breakFinishTime", item.breakFinishTime)
            put("comments", item.comments)
            put("updatedAt", item.updatedAt)
        })
    }
}

private fun weeksToJson(items: List<TimesheetWeek>) =
    JSONArray().apply {
    items.forEach { item ->
        put(JSONObject().apply {
            put("weekStarting", item.weekStarting)
            put("isSubmitted", item.isSubmitted)
            put("isLocked", item.isLocked)
            put("lastEmailedAt", item.lastEmailedAt)
            put("updatedAt", item.updatedAt)
        })
    }
}

private fun daysToJson(items: List<TimesheetDay>) =
    JSONArray().apply {
    items.forEach { item ->
        put(JSONObject().apply {
            put("id", item.id)
            put("employeeName", item.employeeName)
            put("weekStarting", item.weekStarting)
            put("date", item.date)
            put("comments", item.comments)
            put("updatedAt", item.updatedAt)
        })
    }
}

private fun shiftsToJson(items: List<Shift>) =
    JSONArray().apply {
    items.forEach { item ->
        put(JSONObject().apply {
            put("id", item.id)
            put("dayId", item.dayId)
            put("shiftNumber", item.shiftNumber)
            put("startTime", item.startTime)
            put("finishTime", item.finishTime)
            put("updatedAt", item.updatedAt)
        })
    }
}

private fun restBreaksToJson(items: List<RestBreak>) =
    JSONArray().apply {
    items.forEach { item ->
        put(JSONObject().apply {
            put("id", item.id)
            put("shiftId", item.shiftId)
            put("startTime", item.startTime)
            put("finishTime", item.finishTime)
            put("updatedAt", item.updatedAt)
        })
    }
}

private fun JSONArray.toEntries() = objects().map {
    TimesheetEntry(
        id = it.getLong("id"),
        employeeName = it.getString("employeeName"),
        date = it.getString("date"),
        startTime = it.getString("startTime"),
        finishTime = it.getString("finishTime"),
        breakStartTime = it.nullableString("breakStartTime"),
        breakFinishTime = it.nullableString("breakFinishTime"),
        comments = it.getString("comments"),
        updatedAt = it.getLong("updatedAt")
    )
}

private fun JSONArray.toWeeks() = objects().map {
    TimesheetWeek(
        weekStarting = it.getString("weekStarting"),
        isSubmitted = it.getBoolean("isSubmitted"),
        isLocked = it.getBoolean("isLocked"),
        lastEmailedAt = it.nullableLong("lastEmailedAt"),
        updatedAt = it.getLong("updatedAt")
    )
}

private fun JSONArray.toDays() = objects().map {
    TimesheetDay(
        id = it.getLong("id"),
        employeeName = it.getString("employeeName"),
        weekStarting = it.getString("weekStarting"),
        date = it.getString("date"),
        comments = it.getString("comments"),
        updatedAt = it.getLong("updatedAt")
    )
}

private fun JSONArray.toShifts() = objects().map {
    Shift(
        id = it.getLong("id"),
        dayId = it.getLong("dayId"),
        shiftNumber = it.getInt("shiftNumber"),
        startTime = it.getString("startTime"),
        finishTime = it.nullableString("finishTime"),
        updatedAt = it.getLong("updatedAt")
    )
}

private fun JSONArray.toRestBreaks() = objects().map {
    RestBreak(
        id = it.getLong("id"),
        shiftId = it.getLong("shiftId"),
        startTime = it.getString("startTime"),
        finishTime = it.nullableString("finishTime"),
        updatedAt = it.getLong("updatedAt")
    )
}

private fun JSONArray.objects(): List<JSONObject> {
    return (0 until length()).map { getJSONObject(it) }
}

private fun JSONObject.nullableString(name: String): String? {
    return if (isNull(name)) null else getString(name)
}

private fun JSONObject.nullableLong(name: String): Long? {
    return if (isNull(name)) null else getLong(name)
}
