package com.ade.habittracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image // Import Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource // Import painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.model.Achievement
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementItem(
    achievement: Achievement,
    onClick: () -> Unit
) {
    val borderColor = if (achievement.isUnlocked) AccentYellow else CardBackground
    // Jika belum unlock, kita buat gambarnya agak hitam putih/transparan
    val imageAlpha = if (achievement.isUnlocked) 1f else 0.4f

    val progress = if (achievement.goal > 0) achievement.progress.toFloat() / achievement.goal.toFloat() else 0f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(2.dp, borderColor),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF48484A)),
                    contentAlignment = Alignment.Center
                ) {
                    // --- PERUBAHAN UTAMA DI SINI ---
                    Image(
                        painter = painterResource(id = achievement.iconResId), // Mengambil dari ID
                        contentDescription = "Icon Achievement",
                        contentScale = ContentScale.Crop, // Agar gambar memenuhi lingkaran
                        alpha = imageAlpha, // Redup jika belum unlock
                        modifier = Modifier.matchParentSize()
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(achievement.title, color = TextColorPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

                    if (!achievement.isUnlocked && achievement.goal > 1) {
                        Text(
                            "${achievement.progress} / ${achievement.goal}",
                            color = TextColorSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                if (achievement.isUnlocked) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Tercapai",
                        tint = AccentYellow,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (!achievement.isUnlocked && achievement.goal > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = AccentYellow,
                    trackColor = Color(0xFF48484A)
                )
            }
        }
    }
}