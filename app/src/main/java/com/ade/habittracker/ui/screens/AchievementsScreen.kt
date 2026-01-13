package com.ade.habittracker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.style.TextAlign
import com.ade.habittracker.model.Achievement
import com.ade.habittracker.ui.components.AchievementDescriptionDialog
import com.ade.habittracker.ui.components.FallingSnowEffect
import com.ade.habittracker.ui.theme.*


@Composable
fun AchievementsScreen(
    achievements: List<Achievement>
) {
    var achievementToShowDesc by remember { mutableStateOf<Achievement?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

        FallingSnowEffect(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .zIndex(1f)
        ) {

            Text(
                text = "PENCAPAIAN",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextColorPrimary,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(achievements, key = { it.id }) { achievement ->

                    val glowAlpha by animateFloatAsState(
                        targetValue = if (achievement.isUnlocked) 0.40f else 0f,
                        label = "glowAlpha"
                    )

                    val dimAlpha by animateFloatAsState(
                        targetValue = if (achievement.isUnlocked) 1f else 0.45f,
                        label = "dimAlpha"
                    )

                    val labelColor by animateColorAsState(
                        targetValue =
                            if (achievement.isUnlocked) TextColorPrimary
                            else TextColorSecondary,
                        label = "labelColor"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { achievementToShowDesc = achievement },
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .graphicsLayer {
                                        shadowElevation =
                                            if (achievement.isUnlocked) 18.dp.toPx() else 0f
                                    }
                                    .alpha(dimAlpha)
                            ) {
                                if (achievement.isUnlocked) {
                                    // cahaya kuning samar di belakang gambar
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(
                                                AccentYellow.copy(alpha = glowAlpha),
                                                CircleShape
                                            )
                                    )
                                }

                                Image(
                                    painter = painterResource(id = achievement.iconResId),
                                    contentDescription = achievement.title,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = achievement.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = labelColor
                            )

                            Text(
                                text = achievement.description,
                                fontSize = 12.sp,
                                color = labelColor.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center,            // ⭐ rata tengah
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            if (achievement.goal > 1) {
                                val progressSingle =
                                    (achievement.progress.toFloat() / achievement.goal.toFloat())
                                        .coerceIn(0f, 1f)

                                LinearProgressIndicator(
                                    progress = { progressSingle },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = AccentYellow,
                                    trackColor = Color(0xFF48484A)
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    "${achievement.progress}/${achievement.goal}",
                                    fontSize = 11.sp,
                                    color = TextColorSecondary
                                )
                            } else {
                                Text(
                                    text = if (achievement.isUnlocked) "Selesai ✓" else "Terkunci",
                                    fontSize = 12.sp,
                                    color = if (achievement.isUnlocked) AccentYellow else TextColorSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    achievementToShowDesc?.let {
        AchievementDescriptionDialog(
            achievement = it,
            onDismiss = { achievementToShowDesc = null }
        )
    }
}
