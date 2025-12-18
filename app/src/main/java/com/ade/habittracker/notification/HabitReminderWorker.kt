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
import com.ade.habittracker.MainActivity // Pastikan ini sesuai dengan nama Activity utama Anda
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
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "habit_reminder_channel"

        // 1. SETUP INTENT (Agar aplikasi terbuka saat notifikasi diklik)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Flag Immutable wajib untuk Android 12 ke atas
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 2. DATA KATA-KATA MOTIVASI (Agar notifikasi bervariasi)
        val titles = listOf(
            "Waktunya Level Up! ⚔️",
            "Jangan Putus Streak! 🔥",
            "Misi Harian Menanti 📜",
            "Jadilah Lebih Baik 💪",
            "Ingat Tujuanmu 🎯"
        )

        val messages = listOf(
            "Satu langkah kecil hari ini adalah lompatan besar untuk masa depan.",
            "Disiplin adalah jembatan antara tujuan dan pencapaian. Ayo selesaikan misimu!",
            "Jangan biarkan kemalasan menang. Buktikan kamu bisa konsisten!",
            "Streak-mu sedang bagus! Sayang kalau berhenti sekarang.",
            "Kesuksesan dimulai dari kebiasaan sehari-hari. Check-in sekarang!",
            "Avatar-mu butuh XP! Selesaikan habit untuk naik level."
        )

        // Pilih pesan secara acak
        val randomTitle = titles[Random.nextInt(titles.size)]
        val randomMessage = messages[Random.nextInt(messages.size)]

        // 3. BUAT CHANNEL (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel untuk motivasi dan pengingat misi harian"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 4. RAKIT NOTIFIKASI
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.icon) // Ganti dengan ikon notifikasi app Anda
            .setContentTitle(randomTitle)   // Judul acak
            .setContentText(randomMessage) // Pesan acak
            .setStyle(NotificationCompat.BigTextStyle().bigText(randomMessage)) // Agar teks panjang terbaca semua
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Pasang Intent di sini
            .setAutoCancel(true) // Notifikasi hilang otomatis setelah diklik
            .build()

        // Tampilkan Notifikasi (ID 1 agar selalu menimpa notif lama, ganti Random jika ingin menumpuk)
        notificationManager.notify(1, notification)
    }
}