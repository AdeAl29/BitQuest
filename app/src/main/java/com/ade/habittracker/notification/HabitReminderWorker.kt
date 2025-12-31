package com.ade.habittracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ade.habittracker.MainActivity
import com.ade.habittracker.R
import kotlin.random.Random

class HabitReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        sendReminderNotification()
        return Result.success()
    }

    private fun sendReminderNotification() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "habit_reminder_channel"

        // 👉 intent buka app saat notif ditekan
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ======================
        // FIRST TIME CHECK
        // ======================
        val isFirstTime = NotificationPreference.isFirstTime(context)

        val title: String
        val message: String

        if (isFirstTime) {
            title = "Selamat Datang! 🎉"
            message =
                "Perjalanan barumu dimulai hari ini. Satu habit kecil, satu langkah besar."

            NotificationPreference.setNotFirstTime(context)
        } else {
            val titles = listOf(
                "Misi Hari Ini Menunggu ☀️",
                "Jangan Kendur 🔥",
                "XP Menunggumu ⚔️",
                "Masih Ada Waktu ⏳",
                "Disiplin Dikit Lagi 💪",
                "Avatar-mu Butuh Progress 😤",
                "Satu Habit Saja 🎯",
                "Hari Ini Jangan Kosong 📜"
            )

            val messages = listOf(
                "Kerjakan satu habit. Satu itu cukup.",
                "Streak-mu terlalu berharga buat dihentikan.",
                "Sedikit progres hari ini lebih baik daripada nol.",
                "Bukan soal mood. Ini soal komitmen.",
                "XP tidak datang sendiri.",
                "Hari ini masih bisa diselamatkan.",
                "Jangan nunggu semangat, mulai aja dulu.",
                "Satu checklist lagi, habis itu bebas."
            )

            title = titles.random()
            message = messages.random()
        }

        // ======================
        // NOTIFICATION CHANNEL
        // ======================
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_HIGH // 🔥 PASTI MUNCUL
            ).apply {
                description = "Pengingat habit harian"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // ======================
        // BUILD NOTIFICATION
        // ======================
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Random.nextInt(), notification)
    }
}
