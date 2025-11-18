package com.ade.habittracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.model.Achievement // <-- Menggunakan model KOMPLEKS
import com.ade.habittracker.ui.components.AchievementDescriptionDialog
import com.ade.habittracker.ui.components.AchievementItem
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary

@Composable
fun AchievementsScreen(achievements: List<Achievement>) {
    var achievementToShowDesc by remember { mutableStateOf<Achievement?>(null) }

    // Kalkulasi Progress (STATISTIK KESELURUHAN)
    val unlockedCount = achievements.count { it.isUnlocked }
    val totalCount = achievements.size
    val progress = if (totalCount > 0) unlockedCount.toFloat() / totalCount.toFloat() else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "PENCAPAIAN",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextColorPrimary,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }

        // Header Progress Card (Keseluruhan)
        if (totalCount > 0) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "PROGRESS PENCAPAIAN",
                            color = TextColorSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "$unlockedCount dari $totalCount Selesai",
                            color = TextColorPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = AccentYellow,
                            trackColor = Color(0xFF48484A)
                        )
                    }
                }
            }
        }

        if (achievements.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada pencapaian tersedia.", color = TextColorSecondary)
                }
            }
        } else {
            // Menggunakan ID String sebagai key
            items(achievements, key = { it.id }) { achievement ->
                AchievementItem(
                    achievement = achievement,
                    onClick = {
                        achievementToShowDesc = achievement
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (achievementToShowDesc != null) {
        AchievementDescriptionDialog(
            achievement = achievementToShowDesc!!,
            onDismiss = { achievementToShowDesc = null }
        )
    }
}