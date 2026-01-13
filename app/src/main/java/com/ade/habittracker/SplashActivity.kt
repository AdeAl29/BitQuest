package com.ade.habittracker

import android.content.Context // <-- PENTING: Untuk akses SharedPreferences
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Membuat video menjadi full screen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            VideoSplashScreen {
                // Saat video selesai/skip, jalankan pengecekan login
                checkLoginAndNavigate()
            }
        }
    }

    private fun checkLoginAndNavigate() {
        // Mencegah pemanggilan ganda
        if (isFinishing) return

        // --- LOGIKA BARU DI SINI ---
        // Cek data session di memori HP
        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false) // Default false (belum login)

        if (isLoggedIn) {
            // Jika sudah login, langsung ke menu utama
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // Jika belum login, ke halaman login dulu
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish() // Tutup SplashActivity
    }
}

@Composable
fun VideoSplashScreen(onVideoEnded: () -> Unit) {
    val context = LocalContext.current
    val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.splash_video}")

    LaunchedEffect(key1 = true) {
        // Timer pengaman (misal video macet atau terlalu panjang, max 5 detik skip)
        delay(5000L)
        onVideoEnded()
    }

    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(videoUri)

                setOnPreparedListener { mp ->
                    // Agar video full screen (Center Crop)
                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                    // Volume mengikuti pengaturan HP user (tidak di-mute paksa)
                }

                // Jika video selesai diputar secara alami
                setOnCompletionListener {
                    onVideoEnded()
                }

                // Jika terjadi error saat memutar video, langsung skip
                setOnErrorListener { _, _, _ ->
                    onVideoEnded()
                    true
                }

                start()
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}