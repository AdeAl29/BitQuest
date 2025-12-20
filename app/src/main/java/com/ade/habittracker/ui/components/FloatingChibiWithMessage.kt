package com.ade.habittracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun FloatingChibiWithMessage(
    chibiResList: List<Int>,
    messages: List<String>,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    var alignment by remember { mutableStateOf(Alignment.BottomEnd) }
    var currentMessage by remember { mutableStateOf("") }
    var currentChibi by remember { mutableStateOf(chibiResList.first()) }

    LaunchedEffect(Unit) {
        while (true) {
            alignment = listOf(
                Alignment.CenterStart,
                Alignment.CenterEnd,
                Alignment.BottomStart,
                Alignment.BottomEnd
            ).random()

            currentMessage = messages.random()
            currentChibi = chibiResList.random()

            visible = true
            delay(3000L) // tampil

            visible = false
            delay(2000L) // jeda sebelum muncul lagi
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(12.dp)
            ) {

                // 💬 Bubble text
                Box(
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = currentMessage,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Image(
                    painter = painterResource(id = currentChibi),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}
