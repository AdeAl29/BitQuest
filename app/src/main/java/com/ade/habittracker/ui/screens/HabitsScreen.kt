package com.ade.habittracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ade.habittracker.R
import com.ade.habittracker.model.Habit
import com.ade.habittracker.ui.components.*
import com.ade.habittracker.ui.theme.*

enum class HabitFilterMode(val label: String) {
    NONE("Tanpa Filter"),
    EASY_TO_HARD("Ringan → Berat"),
    HARD_TO_EASY("Berat → Ringan")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    habits: List<Habit>,
    onHabitCheckedChanged: (Habit, Boolean) -> Unit,
    onEditClick: (Habit) -> Unit,
    onDeleteClick: (Habit) -> Unit
) {
    var expandedMenuHabitId by remember { mutableStateOf<Int?>(null) }

    // Filter
    var filterMenuExpanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(HabitFilterMode.NONE) }

    // Panduan
    var showGuide by remember { mutableStateOf(false) }

    // Achievement toast
    var achievementMessage by remember { mutableStateOf<String?>(null) }
    var achievementXp by remember { mutableStateOf(0) }

    // Sorting
    val sortedHabits = remember(habits, selectedFilter) {
        when (selectedFilter) {
            HabitFilterMode.NONE -> habits
            HabitFilterMode.EASY_TO_HARD -> habits.sortedBy { it.weight }
            HabitFilterMode.HARD_TO_EASY -> habits.sortedByDescending { it.weight }
        }
    }

    // Chibi voice messages
    val chibiMessages = listOf(
        ChibiMessage("Ayo satu misi lagi!", R.raw.chibi_ayo_satu_misi),
        ChibiMessage("Jangan bolos ya!", R.raw.chibi_jangan_bolos),
        ChibiMessage("Konsisten itu keren banget!", R.raw.chibi_konsisten_keren),
        ChibiMessage("Aku liatin loh 👀", R.raw.chibi_aku_liatin),
        ChibiMessage("Sedikit lagi beres kok!", R.raw.chibi_sedikit_lagi)
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // ❄ background
        FallingSnowEffect(
            modifier = Modifier.fillMaxSize().zIndex(0f)
        )

        // 📋 Konten utama
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).zIndex(1f)
        ) {
            // 🔹 Header + buttons kanan atas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "DAFTAR MISI HARIAN",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextColorPrimary
                )

                Row {

                    // ❓ Tombol panduan
                    IconButton(onClick = { showGuide = true }) {
                        Icon(Icons.Default.Help, "Panduan", tint = AccentYellow)
                    }

                    // 🔽 Tombol filter
                    IconButton(onClick = { filterMenuExpanded = true }) {
                        Icon(Icons.Default.FilterList, "Filter", tint = AccentYellow)
                    }

                    // Menu filter
                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false },
                        containerColor = CardBackground
                    ) {
                        HabitFilterMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        mode.label,
                                        color =
                                            if (selectedFilter == mode) AccentYellow
                                            else TextColorPrimary,
                                        fontWeight =
                                            if (selectedFilter == mode) FontWeight.Bold
                                            else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedFilter = mode
                                    filterMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (sortedHabits.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada misi. Tambahkan satu!", color = TextColorSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(sortedHabits, key = { it.id }) { habit ->
                        HabitItem(
                            habit = habit,
                            onCheckedChanged = { checked ->
                                if (!habit.isCompleted && checked) {
                                    achievementMessage =
                                        "Keren! kamu menyelesaikan '${habit.name}'"
                                    achievementXp = habit.weight * 1
                                }
                                onHabitCheckedChanged(habit, checked)
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

        // 🧸 Chibi draggable
        DraggableChibiWithBubble(
            chibiRes = R.drawable.chibi_helper,
            messages = chibiMessages,
            modifier = Modifier.fillMaxSize().zIndex(50f)
        )

        // 🏆 Achievement toast
        achievementMessage?.let {
            AchievementToast(
                title = "Misi Selesai!",
                description = it,
                xp = achievementXp,
                onDismiss = { achievementMessage = null }
            )
        }

        // 📘 Panduan dialog
        if (showGuide) {
            AlertDialog(
                onDismissRequest = { showGuide = false },
                confirmButton = {
                    TextButton(onClick = { showGuide = false }) {
                        Text("Mengerti!", color = AccentYellow)
                    }
                },
                title = {
                    Text("PANDUAN APLIKASI",
                        color = TextColorPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("• Centang misi untuk menyelesaikan hari ini.", color = TextColorSecondary)
                        Text("• Bobot (weight) menentukan XP & filter.", color = TextColorSecondary)
                        Text("• Tekan titik tiga untuk edit / hapus.", color = TextColorSecondary)
                        Text("• Tekan chibi untuk pesan + suara.", color = TextColorSecondary)
                        Text("• Gunakan filter untuk urutkan misi.", color = TextColorSecondary)
                        Text("• XP & level naik sesuai konsistensi.", color = TextColorSecondary)
                        Text("• Buka tiap hari agar streak tidak hilang!", color = TextColorSecondary)
                    }
                },
                containerColor = CardBackground
            )
        }
    }
}
