package com.ade.habittracker

import android.Manifest
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ade.habittracker.notification.HabitReminderWorker
import com.ade.habittracker.ui.navigation.MainScreen
import com.ade.habittracker.ui.theme.DarkBackground
import com.ade.habittracker.ui.theme.HabitTrackerTheme
import com.ade.habittracker.ui.viewmodel.HabitViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: HabitViewModel by viewModels()

    // 🔊 MUSIK LATAR
    private var mediaPlayer: MediaPlayer? = null

    // Status Izin Musik (Default True, nanti diupdate dari AppData)
    private var isMusicAllowed = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 1. MENGAKTIFKAN MODE FULLSCREEN (EDGE-TO-EDGE) 🔥
        enableEdgeToEdge()

        setContent {
            HabitTrackerTheme {
                // 🔥 2. SURFACE BACKGROUND 🔥
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    MainScreen(
                        viewModel = viewModel,
                        onScheduleReminderClick = {
                            askNotificationPermission()
                        }
                    )
                }
            }
        }

        // 🔥 3. OBSERVER PENGATURAN MUSIK 🔥
        // Memantau perubahan setting di AppData secara Real-time
        lifecycleScope.launch {
            viewModel.appData.collectLatest { data ->
                if (data != null) {
                    // Update status lokal
                    isMusicAllowed = data.isMusicEnabled

                    // Aksi langsung saat tombol switch ditekan
                    if (isMusicAllowed) {
                        // Jika diaktifkan, coba mulai musik (hanya jika activity sedang aktif)
                        // Cek state lifecycle agar tidak nyala saat background
                        if (lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
                            startBackgroundMusic()
                        }
                    } else {
                        // Jika dimatikan, pause musik
                        pauseBackgroundMusic()
                    }
                }
            }
        }
    }

    // --- LOGIKA LIFECYCLE & MUSIK ---

    override fun onStart() {
        super.onStart()

        // 📆 Reset habit harian & Cek Season Bulanan
        viewModel.resetHabitsIfNewDay()

        // 🔊 Musik mulai (Hanya jika diizinkan di setting)
        if (isMusicAllowed) {
            startBackgroundMusic()
        }

        // 🔔 NOTIFIKASI WORKER SAAT APP DIBUKA
        val oneTimeWork = OneTimeWorkRequestBuilder<HabitReminderWorker>().build()
        WorkManager.getInstance(this).enqueue(oneTimeWork)
    }

    override fun onStop() {
        super.onStop()
        // Pause musik saat aplikasi diminimize/keluar
        pauseBackgroundMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Bersihkan resource musik saat aplikasi dimatikan total
        releaseMediaPlayer()
    }

    // --- FUNGSI AUDIO PLAYER ---

    private fun startBackgroundMusic() {
        // Guard Clause: Jika setting musik mati, jangan jalankan apapun
        if (!isMusicAllowed) return

        if (mediaPlayer == null) {
            // Pastikan file 'sountrack' ada di folder res/raw
            mediaPlayer = MediaPlayer.create(this, R.raw.sountrack).apply {
                isLooping = true // Musik berulang
                setVolume(0.5f, 0.5f) // Volume 50%
            }
        }
        try {
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            Log.e("MainActivityMusic", "MediaPlayer error: ${e.message}")
            releaseMediaPlayer()
            // Retry logic sederhana (hindari loop crash)
            try {
                mediaPlayer = MediaPlayer.create(this, R.raw.sountrack)
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
            } catch (e2: Exception) {
                Log.e("MainActivityMusic", "Retry failed")
            }
        }
    }

    private fun pauseBackgroundMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
    }

    // --- LOGIKA IZIN NOTIFIKASI (Android 13+) ---

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
            // Untuk Android 12 ke bawah, izin otomatis diberikan saat install
            viewModel.scheduleDailyReminder(applicationContext)
            Toast.makeText(this, "Pengingat harian diaktifkan!", Toast.LENGTH_SHORT).show()
        }
    }
}