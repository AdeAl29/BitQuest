package com.ade.habittracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ade.habittracker.R

class HabitReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Panggil fungsi untuk menampilkan notifikasi
        sendReminderNotification()

        // Tandai pekerjaan sebagai sukses
        return Result.success()
    }

    private fun sendReminderNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "habit_reminder_channel"

        // Buat Channel (wajib untuk Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel untuk pengingat misi harian"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Buat Notifikasi
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ganti dengan ikon notifikasi Anda
            .setContentTitle("Jangan Lupa Misi Harianmu!")
            .setContentText("Ayo selesaikan misi hari ini dan lanjutkan streak-mu!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Menutup notifikasi saat di-tap
            .build()

        // Tampilkan Notifikasi
        notificationManager.notify(1, notification)
    }
}