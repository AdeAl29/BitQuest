package com.ade.habittracker

import android.Manifest
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ade.habittracker.notification.HabitReminderWorker
import com.ade.habittracker.ui.navigation.MainScreen
import com.ade.habittracker.ui.theme.HabitTrackerTheme
import com.ade.habittracker.ui.viewmodel.HabitViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HabitViewModel by viewModels()

    // 🔊 MUSIK LATAR
    private var mediaPlayer: MediaPlayer? = null

    private fun startBackgroundMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.sountrack).apply {
                isLooping = true
                setVolume(0.5f, 0.5f)
            }
        }
        try {
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        } catch (e: IllegalStateException) {
            Log.e("MainActivityMusic", "MediaPlayer error: ${e.message}")
            releaseMediaPlayer()
            startBackgroundMusic()
        }
    }

    private fun pauseBackgroundMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onStart() {
        super.onStart()

        // 🔊 Musik mulai
        startBackgroundMusic()

        // 📆 Reset habit harian
        viewModel.resetHabitsIfNewDay()

        // 🔔 NOTIFIKASI SAAT APP DIBUKA
        val oneTimeWork = OneTimeWorkRequestBuilder<HabitReminderWorker>().build()
        WorkManager.getInstance(this).enqueue(oneTimeWork)
    }

    override fun onStop() {
        super.onStop()
        pauseBackgroundMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }

    // 🔔 REQUEST IZIN NOTIFIKASI (Android 13+)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.scheduleDailyReminder(applicationContext)
                Toast.makeText(this, "Pengingat harian diaktifkan!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Izin notifikasi ditolak.", Toast.LENGTH_SHORT).show()
            }
        }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.scheduleDailyReminder(applicationContext)
            Toast.makeText(this, "Pengingat harian diaktifkan!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HabitTrackerTheme {
                MainScreen(
                    viewModel = viewModel,
                    onScheduleReminderClick = {
                        askNotificationPermission()
                    }
                )
            }
        }
    }
}
