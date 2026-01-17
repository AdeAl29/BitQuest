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

// Enum untuk Mode Filter/Sorting
enum class HabitFilterMode(val label: String) {
    NONE("Tanpa Filter"),
    EASY_TO_HARD("Ringan → Berat"),
    HARD_TO_EASY("Berat → Ringan")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    habits: List<Habit>,
    // 🔥 PARAMETER BARU: Status Chibi (ON/OFF)
    isChibiEnabled: Boolean = true,
    onHabitCheckedChanged: (Habit, Boolean) -> Unit,
    onEditClick: (Habit) -> Unit,
    onDeleteClick: (Habit) -> Unit
) {
    // State untuk Menu Dropdown pada setiap item Habit
    var expandedMenuHabitId by remember { mutableStateOf<Int?>(null) }

    // State untuk Menu Filter
    var filterMenuExpanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(HabitFilterMode.NONE) }

    // State untuk Dialog Panduan
    var showGuide by remember { mutableStateOf(false) }

    // --- STATE KOMUNIKASI KE CHIBI ---
    var chibiNotification by remember { mutableStateOf<String?>(null) }

    // Logika Sorting List berdasarkan Filter yang dipilih
    val sortedHabits = remember(habits, selectedFilter) {
        when (selectedFilter) {
            HabitFilterMode.NONE -> habits
            HabitFilterMode.EASY_TO_HARD -> habits.sortedBy { it.weight }
            HabitFilterMode.HARD_TO_EASY -> habits.sortedByDescending { it.weight }
        }
    }

    // Daftar Suara & Pesan Random Chibi
    val chibiMessages = listOf(
        ChibiMessage("Ayo satu misi lagi!", R.raw.chibi_ayo_satu_misi),
        ChibiMessage("Jangan bolos ya!", R.raw.chibi_jangan_bolos),
        ChibiMessage("Konsisten itu keren banget!", R.raw.chibi_konsisten_keren),
        ChibiMessage("Aku liatin loh 👀", R.raw.chibi_aku_liatin),
        ChibiMessage("Sedikit lagi beres kok!", R.raw.chibi_sedikit_lagi)
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. Background Efek Salju
        FallingSnowEffect(
            modifier = Modifier.fillMaxSize().zIndex(0f)
        )

        // 2. Konten Utama (List & Header)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .zIndex(1f)
        ) {
            // --- HEADER & TOMBOL ACTION ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DAFTAR MISI HARIAN",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextColorPrimary
                )

                Row {
                    // Tombol Panduan
                    IconButton(onClick = { showGuide = true }) {
                        Icon(Icons.Default.Help, "Panduan", tint = AccentYellow)
                    }

                    // Tombol Filter
                    Box {
                        IconButton(onClick = { filterMenuExpanded = true }) {
                            Icon(Icons.Default.FilterList, "Filter", tint = AccentYellow)
                        }

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
                                            color = if (selectedFilter == mode) AccentYellow else TextColorPrimary,
                                            fontWeight = if (selectedFilter == mode) FontWeight.Bold else FontWeight.Normal
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
            }

            Spacer(Modifier.height(12.dp))

            // --- LIST HABIT ---
            if (sortedHabits.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada misi. Tambahkan satu!", color = TextColorSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(sortedHabits, key = { it.id }) { habit ->
                        HabitItem(
                            habit = habit,
                            onCheckedChanged = { checked ->
                                if (!habit.isCompleted && checked) {
                                    // Kirim notifikasi ke Chibi (jika aktif)
                                    chibiNotification = "Keren! Misi Selesai!\n+${habit.weight} XP"
                                }
                                onHabitCheckedChanged(habit, checked)
                            },
                            isMenuExpanded = expandedMenuHabitId == habit.id,
                            onMenuClick = {
                                expandedMenuHabitId = if (expandedMenuHabitId == habit.id) null else habit.id
                            },
                            onDismissMenu = { expandedMenuHabitId = null },
                            onEditClick = { onEditClick(habit) },
                            onDeleteClick = { onDeleteClick(habit) }
                        )
                    }
                }
            }
        }

        // 3. CHIBI ASSISTANT (Hanya muncul jika Enabled)
        if (isChibiEnabled) {
            DraggableChibiWithBubble(
                chibiRes = R.drawable.chibi_helper, // Kembali ke resource default
                messages = chibiMessages,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(50f),
                externalMessage = chibiNotification,
                onExternalMessageDismiss = { chibiNotification = null }
            )
        }

        // 4. DIALOG PANDUAN
        if (showGuide) {
            AlertDialog(
                onDismissRequest = { showGuide = false },
                confirmButton = {
                    TextButton(onClick = { showGuide = false }) {
                        Text("Mengerti!", color = AccentYellow)
                    }
                },
                title = {
                    Text("PANDUAN APLIKASI", color = TextColorPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("• Centang misi untuk menyelesaikan.", color = TextColorSecondary)
                        Text("• Misi selesai tidak bisa dibatalkan.", color = TextColorSecondary)
                        Text("• Chibi bisa dimatikan di menu Pengaturan.", color = TextColorSecondary)
                    }
                },
                containerColor = CardBackground
            )
        }
    }
}