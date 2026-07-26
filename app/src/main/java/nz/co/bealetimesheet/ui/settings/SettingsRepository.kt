package nz.co.bealetimesheet.ui.settings

import android.content.Context

object SettingsRepository {

    private const val PREFERENCES_NAME =
        "beale_timesheet_preferences"

    private const val KEY_EMPLOYEE_NAME =
        "employee_name"

    private const val KEY_RECIPIENT_EMAIL =
        "last_recipient_email"

    private const val KEY_TUESDAY_REMINDER =
        "tuesday_submission_reminder"

    private const val KEY_ACTIVE_SHIFT_REMINDER =
        "active_shift_reminder"

    private const val DEFAULT_EMPLOYEE_NAME =
        "Brad Pledger"

    private const val DEFAULT_RECIPIENT_EMAIL =
        "anna.bealeloggers@gmail.com"

    fun getEmployeeName(
        context: Context
    ): String {
        return preferences(context).getString(
            KEY_EMPLOYEE_NAME,
            DEFAULT_EMPLOYEE_NAME
        ) ?: DEFAULT_EMPLOYEE_NAME
    }

    fun saveEmployeeName(
        context: Context,
        employeeName: String
    ) {
        preferences(context)
            .edit()
            .putString(
                KEY_EMPLOYEE_NAME,
                employeeName.trim()
            )
            .apply()
    }

    fun getRecipientEmail(
        context: Context
    ): String {
        return preferences(context).getString(
            KEY_RECIPIENT_EMAIL,
            DEFAULT_RECIPIENT_EMAIL
        ) ?: DEFAULT_RECIPIENT_EMAIL
    }

    fun saveRecipientEmail(
        context: Context,
        recipientEmail: String
    ) {
        preferences(context)
            .edit()
            .putString(
                KEY_RECIPIENT_EMAIL,
                recipientEmail.trim()
            )
            .apply()
    }

    fun getTuesdayReminderEnabled(context: Context): Boolean {
        return preferences(context).getBoolean(
            KEY_TUESDAY_REMINDER,
            true
        )
    }

    fun saveTuesdayReminderEnabled(
        context: Context,
        enabled: Boolean
    ) {
        preferences(context)
            .edit()
            .putBoolean(KEY_TUESDAY_REMINDER, enabled)
            .apply()
    }

    fun getActiveShiftReminderEnabled(context: Context): Boolean {
        return preferences(context).getBoolean(
            KEY_ACTIVE_SHIFT_REMINDER,
            true
        )
    }

    fun saveActiveShiftReminderEnabled(
        context: Context,
        enabled: Boolean
    ) {
        preferences(context)
            .edit()
            .putBoolean(KEY_ACTIVE_SHIFT_REMINDER, enabled)
            .apply()
    }

    private fun preferences(
        context: Context
    ) = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )
}
