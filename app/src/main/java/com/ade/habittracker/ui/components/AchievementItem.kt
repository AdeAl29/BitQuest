package com.ade.habittracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.model.Achievement
import com.ade.habittracker.ui.theme.*

@Composable
fun AchievementItem(
    achievement: Achievement,
    onClick: () -> Unit
) {
    val borderColor = if (achievement.isUnlocked) AccentYellow else CardBackground
    val imageAlpha = if (achievement.isUnlocked) 1f else 0.4f
    val progress =
        if (achievement.goal > 0) achievement.progress.toFloat() / achievement.goal.toFloat() else 0f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(2.dp, borderColor),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)    // 🔥 ukuran seragam untuk semua item
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // --- ICON / AVATAR ---
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF48484A)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = achievement.iconResId),
                    contentDescription = achievement.title,
                    contentScale = ContentScale.Crop,
                    alpha = imageAlpha,
                    modifier = Modifier.matchParentSize()
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // --- TEXT & PROGRESS ---
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    achievement.title,
                    color = TextColorPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )

                // ruang kecil agar rapi
                Spacer(modifier = Modifier.height(6.dp))

                if (!achievement.isUnlocked && achievement.goal > 1) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = AccentYellow,
                        trackColor = Color(0xFF48484A)
                    )
                } else if (!achievement.isUnlocked) {
                    Text(
                        "Terkunci",
                        color = TextColorSecondary,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        "Selesai ✓",
                        color = AccentYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // --- CHECK ICON (UNLOCKED) ---
            if (achievement.isUnlocked) {
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Unlocked",
                    tint = AccentYellow,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
