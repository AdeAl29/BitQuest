package com.ade.habittracker.notification

import android.content.Context
import androidx.work.*

import java.util.concurrent.TimeUnit

object HabitReminderScheduler {

    fun scheduleDailyReminder(context: Context) {

        val workRequest =
            PeriodicWorkRequestBuilder<HabitReminderWorker>(
                12, TimeUnit.HOURS // ⏰ 2x sehari (pagi & malam)
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "habit_daily_reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
