package com.ade.habittracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ade.habittracker.R
import com.ade.habittracker.model.Habit
import com.ade.habittracker.ui.components.ChibiMessage
import com.ade.habittracker.ui.components.DraggableChibiWithBubble
import com.ade.habittracker.ui.components.FallingSnowEffect
import com.ade.habittracker.ui.components.HabitItem
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary

@Composable
fun HabitsScreen(
    habits: List<Habit>,
    onHabitCheckedChanged: (Habit, Boolean) -> Unit,
    onEditClick: (Habit) -> Unit,
    onDeleteClick: (Habit) -> Unit
) {
    var expandedMenuHabitId by remember { mutableStateOf<Int?>(null) }

    // 🔗 TEKS + SUARA (SATU PAKET, TIDAK BISA KETUKER)
    val chibiMessages = listOf(
        ChibiMessage(
            text = "Ayo satu misi lagi!",
            voiceRes = R.raw.chibi_ayo_satu_misi
        ),
        ChibiMessage(
            text = "Jangan bolos ya!",
            voiceRes = R.raw.chibi_jangan_bolos
        ),
        ChibiMessage(
            text = "Konsisten dikit lagi!",
            voiceRes = R.raw.chibi_konsisten_keren
        ),
        ChibiMessage(
            text = "Aku liatin loh 👀",
            voiceRes = R.raw.chibi_aku_liatin
        ),
        ChibiMessage(
            text = "Sedikit lagi selesai!",
            voiceRes = R.raw.chibi_sedikit_lagi
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // ❄️ Layer 1 — Salju (background)
        FallingSnowEffect(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
        )

        // 📋 Layer 2 — Konten utama
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .zIndex(1f)
        ) {

            Text(
                text = "DAFTAR MISI HARIAN",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextColorPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada misi. Tambahkan satu!",
                        color = TextColorSecondary
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        HabitItem(
                            habit = habit,
                            onCheckedChanged = { isChecked ->
                                onHabitCheckedChanged(habit, isChecked)
                            },
                            isMenuExpanded = expandedMenuHabitId == habit.id,
                            onMenuClick = {
                                expandedMenuHabitId =
                                    if (expandedMenuHabitId == habit.id) null else habit.id
                            },
                            onDismissMenu = { expandedMenuHabitId = null },
                            onEditClick = { onEditClick(habit) },
                            onDeleteClick = { onDeleteClick(habit) }
                        )
                    }
                }
            }
        }

        // 👀 Layer 3 — CHIBI DIAM, DRAG, TAP = 1 PESAN + 1 SUARA
        DraggableChibiWithBubble(
            chibiRes = R.drawable.chibi_helper,
            messages = chibiMessages,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(30f) // 🔥 PALING DEPAN
        )
    }
}
