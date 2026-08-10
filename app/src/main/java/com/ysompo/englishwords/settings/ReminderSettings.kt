package com.ysompo.englishwords.settings

import android.content.Context

class ReminderSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("reminder_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HOUR = "reminder_hour"
        private const val KEY_MINUTE = "reminder_minute"
        const val DEFAULT_HOUR = 20
        const val DEFAULT_MINUTE = 0
    }

    fun getReminderHour(): Int = prefs.getInt(KEY_HOUR, DEFAULT_HOUR)

    fun getReminderMinute(): Int = prefs.getInt(KEY_MINUTE, DEFAULT_MINUTE)

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit().putInt(KEY_HOUR, hour).putInt(KEY_MINUTE, minute).apply()
    }
}
