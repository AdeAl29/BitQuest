package com.ade.habittracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

@Composable
fun FallingSnowEffect(
    modifier: Modifier = Modifier,
    snowCount: Int = 80
) {
    data class Snowflake(
        var x: Float,
        var y: Float,
        val radius: Float,
        val speed: Float,
        val alpha: Float
    )

    val snowflakes = remember {
        List(snowCount) {
            Snowflake(
                x = Random.nextFloat() * 1080f,
                y = Random.nextFloat() * 2400f,
                radius = Random.nextFloat() * 3.5f + 1.5f,
                speed = Random.nextFloat() * 1.5f + 0.8f,
                alpha = Random.nextFloat() * 0.2f + 0.1f
            )
        }
    }

    // ❄️ FRAME-BASED ANIMATION (HALUS & CONTINUOUS)
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                snowflakes.forEach { snow ->
                    snow.y += snow.speed

                    // reset ke atas kalau keluar layar
                    if (snow.y > 2400f) {
                        snow.y = 0f
                        snow.x = Random.nextFloat() * 1080f
                    }
                }
            }
        }
    }

    Canvas(modifier = modifier) {
        snowflakes.forEach { snow ->
            drawCircle(
                color = Color.White.copy(alpha = snow.alpha),
                radius = snow.radius,
                center = Offset(snow.x, snow.y)
            )
        }
    }
}
