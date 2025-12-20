package com.ade.habittracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun FloatingChibi(
    drawableRes: Int,
    modifier: Modifier = Modifier,
    size: Int = 64 // ukuran chibi (dp)
) {
    var visible by remember { mutableStateOf(false) }
    var alignment by remember { mutableStateOf(Alignment.CenterStart) }

    LaunchedEffect(Unit) {
        while (true) {

            // Pilih sisi layar (kiri / kanan)
            alignment =
                if (Random.nextBoolean())
                    Alignment.CenterStart
                else
                    Alignment.CenterEnd

            visible = true
            delay(2500L) // muncul

            visible = false
            delay(2000L) // jeda sebelum pindah sisi
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = alignment
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = null,
                modifier = Modifier.size(size.dp)
            )
        }
    }
}
