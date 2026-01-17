package com.ade.habittracker

import android.content.Context
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

        // 1. Membuat video menjadi full screen (Immersive)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            // Langsung panggil VideoSplashScreen (Video Default)
            VideoSplashScreen {
                // Saat video selesai/skip, jalankan pengecekan login
                checkLoginAndNavigate()
            }
        }
    }

    private fun checkLoginAndNavigate() {
        // Mencegah pemanggilan ganda jika activity sudah mau tutup
        if (isFinishing) return

        // Cek data session di memori HP (Shared Preferences)
        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false) // Default false

        if (isLoggedIn) {
            // Jika sudah login, langsung ke menu utama
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // Jika belum login, ke halaman login dulu
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Tutup SplashActivity agar tidak bisa kembali (Back) ke sini
        finish()
    }
}

@Composable
fun VideoSplashScreen(onVideoEnded: () -> Unit) {
    val context = LocalContext.current

    // 🔥 Menggunakan Video Default (Karena pengaturan splash dihapus)
    val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.splash_video}")

    LaunchedEffect(key1 = true) {
        // Timer pengaman (max 5 detik skip otomatis jika video macet)
        delay(5000L)
        onVideoEnded()
    }

    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(videoUri)

                setOnPreparedListener { mp ->
                    // Agar video full screen (Center Crop) tanpa gepeng
                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                }

                // Jika video selesai diputar secara alami
                setOnCompletionListener {
                    onVideoEnded()
                }

                // Jika terjadi error saat memutar video, langsung skip agar user tidak terjebak
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