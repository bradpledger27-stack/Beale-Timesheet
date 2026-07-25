package nz.co.bealetimesheet.ui.settings

import android.content.Context

object SettingsRepository {

    private const val PREFERENCES_NAME =
        "beale_timesheet_preferences"

    private const val KEY_EMPLOYEE_NAME =
        "employee_name"

    private const val KEY_RECIPIENT_EMAIL =
        "last_recipient_email"

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

    private fun preferences(
        context: Context
    ) = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )
}