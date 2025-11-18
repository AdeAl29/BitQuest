package com.ade.habittracker

import android.content.Intent
import android.media.MediaPlayer // <-- IMPORT BARU
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // <-- IMPORT BARU
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay // <-- IMPORT BARU

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Membuat video menjadi full screen (menyembunyikan status bar, dll.)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            VideoSplashScreen {
                // Callback ini akan dipanggil saat video selesai ATAU durasi tercapai
                goToMainActivity()
            }
        }
    }

    private fun goToMainActivity() {
        // Mencegah pemanggilan ganda jika video selesai dan timer berjalan bersamaan
        if (isFinishing) return

        startActivity(Intent(this, MainActivity::class.java))
        finish() // Menutup SplashActivity agar tidak bisa kembali
    }
}

@Composable
fun VideoSplashScreen(onVideoEnded: () -> Unit) {
    val context = LocalContext.current
    val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.splash_video1}")

    // LaunchedEffect akan berjalan 1x. Ini akan memanggil onVideoEnded()
    // setelah 2500ms (2.5 detik), tidak peduli durasi videonya.
    LaunchedEffect(key1 = true) {
        delay(2500L) // Atur durasi splash screen di sini (misal: 2.5 detik)
        onVideoEnded()
    }

    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(videoUri)

                setOnPreparedListener { mp ->
                    // VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING adalah "CenterCrop"
                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)

                    // --- PERUBAHAN DI SINI ---
                    // Baris mp.setVolume(0f, 0f) telah dihapus
                    // Video sekarang akan menggunakan volume media default perangkat.
                    // -------------------------
                }

                // Fallback: Jika video selesai LEBIH CEPAT dari 2.5 detik
                setOnCompletionListener {
                    onVideoEnded()
                }

                // Fallback: Langsung skip jika video error
                setOnErrorListener { _, _, _ ->
                    onVideoEnded()
                    true
                }

                start()
            }
        },
        modifier = Modifier.fillMaxSize() // Memastikan VideoView memenuhi layar
    )
}