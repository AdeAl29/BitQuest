package com.ade.habittracker.notification

import android.content.Context

object NotificationPreference {

    private const val PREF_NAME = "habit_notification_pref"
    private const val KEY_FIRST_TIME = "is_first_notification"

    fun isFirstTime(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FIRST_TIME, true)
    }

    fun setNotFirstTime(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_FIRST_TIME, false).apply()
    }
}
