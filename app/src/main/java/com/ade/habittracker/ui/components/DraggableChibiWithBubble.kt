package com.ade.habittracker.ui.components

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

// 🔗 TEKS + SUARA SATU PAKET
data class ChibiMessage(
    val text: String,
    val voiceRes: Int
)

@Composable
fun DraggableChibiWithBubble(
    chibiRes: Int,
    messages: List<ChibiMessage>,
    modifier: Modifier = Modifier,
    autoHideMillis: Long = 2500L
) {
    val context = LocalContext.current

    var offset by remember { mutableStateOf(Offset.Zero) }
    var showBubble by remember { mutableStateOf(false) }
    var currentMessage by remember { mutableStateOf<ChibiMessage?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // ⏱ auto-hide bubble
    LaunchedEffect(showBubble) {
        if (showBubble) {
            delay(autoHideMillis)
            showBubble = false
        }
    }

    // 🧹 bersihkan MediaPlayer
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        offset.x.roundToInt(),
                        offset.y.roundToInt()
                    )
                }
                // ✋ drag
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                    }
                }
                // 👆 tap
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (messages.isNotEmpty()) {
                                val picked = messages.random()
                                currentMessage = picked
                                showBubble = true

                                // 🔊 putar suara SESUAI teks
                                mediaPlayer?.release()
                                mediaPlayer = MediaPlayer.create(
                                    context,
                                    picked.voiceRes
                                )
                                mediaPlayer?.start()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                AnimatedVisibility(
                    visible = showBubble,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.95f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = currentMessage?.text.orEmpty(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Image(
                    painter = painterResource(id = chibiRes),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}
