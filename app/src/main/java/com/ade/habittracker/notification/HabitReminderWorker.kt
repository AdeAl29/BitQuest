package com.ade.habittracker.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ade.habittracker.R

class HabitReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Fungsi untuk menampilkan notifikasi
        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        val channelId = "HABIT_REMINDER_CHANNEL"
        val notificationId = 1

        // Membuat Notification Channel (Wajib untuk Android 8.0+)
        val channel = NotificationChannel(
            channelId,
            "Habit Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Channel for daily habit reminders."
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        // Membuat notifikasi
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ganti dengan ikon notifikasi Anda
            .setContentTitle("Jangan Lupa Misi Hari Ini!")
            .setContentText("Ayo selesaikan misi harianmu dan raih poin XP!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Menampilkan notifikasi
        with(NotificationManagerCompat.from(context)) {
            // Cek permission sebelum menampilkan notifikasi
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Jika tidak ada izin, jangan tampilkan notifikasi.
                // Seharusnya izin sudah diminta di UI.
                return
            }
            notify(notificationId, builder.build())
        }
    }
}
