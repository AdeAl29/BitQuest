package com.ade.habittracker.ui.components

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.R
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.TextColorPrimary
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

data class ChibiMessage(
    val text: String,
    val voiceRes: Int
)

@Composable
fun DraggableChibiWithBubble(
    chibiRes: Int,
    messages: List<ChibiMessage>,
    modifier: Modifier = Modifier,
    // --- PARAMETER BARU (Untuk Integrasi Achievement) ---
    externalMessage: String? = null,      // Pesan dari luar (misal: "Misi Selesai +10XP")
    onExternalMessageDismiss: () -> Unit = {} // Callback saat pesan luar selesai ditampilkan
) {
    val context = LocalContext.current

    // State Posisi (Drag)
    var offset by remember { mutableStateOf(Offset.Zero) }

    // State Pesan Internal (Tap biasa)
    var internalMessage by remember { mutableStateOf<ChibiMessage?>(null) }

    // Logic: Tampilkan pesan eksternal DULUAN jika ada, kalau tidak baru pesan internal
    val activeText = externalMessage ?: internalMessage?.text
    val isBubbleVisible = activeText != null
    val isExternalActive = externalMessage != null

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // 1. EFEK UNTUK PESAN EKSTERNAL (Saat Misi Selesai)
    LaunchedEffect(externalMessage) {
        if (externalMessage != null) {
            // Hapus pesan internal biar gak tabrakan
            internalMessage = null

            // Mainkan suara 'Sukses' (Pastikan file raw ini ada)
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, R.raw.chibi_konsisten_keren)
            mediaPlayer?.start()

            // Tahan pesan achievement selama 3 detik
            delay(3000)

            // Beritahu parent bahwa pesan sudah selesai
            onExternalMessageDismiss()
        }
    }

    // 2. EFEK UNTUK PESAN INTERNAL (Saat Di-Tap)
    LaunchedEffect(internalMessage) {
        if (internalMessage != null) {
            // Tahan pesan tap selama 2.5 detik
            delay(2500)
            internalMessage = null
        }
    }

    // Bersihkan MediaPlayer saat layar ditutup
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // --- UI COMPONENT ---
    Box(modifier = modifier, contentAlignment = Alignment.CenterEnd) {

        Box(
            modifier = Modifier
                .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                // Gesture Drag
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                    }
                }
                // Gesture Tap
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            // Hanya respon tap jika TIDAK sedang menampilkan pesan achievement
                            if (!isExternalActive && messages.isNotEmpty()) {
                                val picked = messages.random()
                                internalMessage = picked

                                mediaPlayer?.release()
                                mediaPlayer = MediaPlayer.create(context, picked.voiceRes)
                                mediaPlayer?.start()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // --- BUBBLE CHAT ---
                AnimatedVisibility(
                    visible = isBubbleVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        color = CardBackground,
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp,
                        // Jika pesan achievement -> Beri border KUNING (Emas)
                        border = if (isExternalActive) BorderStroke(2.dp, AccentYellow) else null
                    ) {
                        Text(
                            text = activeText.orEmpty(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            // Jika pesan achievement -> Teks KUNING (Emas), Normal -> Putih
                            color = if (isExternalActive) AccentYellow else TextColorPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // --- GAMBAR CHIBI ---
                Image(
                    painter = painterResource(id = chibiRes),
                    contentDescription = "Chibi Helper",
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}