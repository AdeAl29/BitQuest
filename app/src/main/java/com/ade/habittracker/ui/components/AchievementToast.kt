package com.ade.habittracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary
import kotlinx.coroutines.delay

@Composable
fun AchievementToast(
    title: String,
    description: String,
    xp: Int,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2500)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 50.dp)
            .zIndex(999f),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .shadow(10.dp, shape = MaterialTheme.shapes.medium)
                .clip(MaterialTheme.shapes.medium),
            color = CardBackground // 💥 warna sama dengan card habit
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "★",
                    fontSize = 26.sp,
                    color = AccentYellow,             // 🌟 warna XP akmu
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextColorPrimary         // 🎯 font utama
                    )

                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = TextColorSecondary       // 👀 warna sekunder
                    )

                    Text(
                        text = "+$xp XP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentYellow
                    )
                }
            }
        }
    }
}
