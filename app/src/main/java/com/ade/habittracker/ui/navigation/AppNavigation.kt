package com.ade.habittracker.ui.navigation

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ade.habittracker.data.predefinedHabitTemplates
import com.ade.habittracker.model.Habit
import com.ade.habittracker.ui.components.DeleteConfirmationDialog
import com.ade.habittracker.ui.components.EditNameDialog
import com.ade.habittracker.ui.components.sheets.*
import com.ade.habittracker.ui.screens.AchievementsScreen
import com.ade.habittracker.ui.screens.HabitsScreen
import com.ade.habittracker.ui.screens.StatsScreen
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.DarkBackground
import com.ade.habittracker.ui.theme.PrimaryColor
import com.ade.habittracker.ui.theme.TextColorSecondary
import com.ade.habittracker.ui.viewmodel.HabitViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: HabitViewModel,
    onScheduleReminderClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appData by viewModel.appData.collectAsStateWithLifecycle()

    // --- PAGER STATE (Untuk Geser Halaman) ---
    // 0 = Habits, 1 = Stats, 2 = Achievements
    val pagerState = rememberPagerState(pageCount = { 3 })

    // State untuk Bottom Sheet & Dialog
    var showAddOptionsSheet by remember { mutableStateOf(false) }
    var showManualAddSheet by remember { mutableStateOf(false) }
    var showTemplateSheet by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    // State untuk Profil
    var showNameEditDialog by remember { mutableStateOf(false) }
    var showAvatarPickerSheet by remember { mutableStateOf(false) }
    var showTitlePickerSheet by remember { mutableStateOf(false) }

    // --- CONTAINER UTAMA (Fullscreen Box) ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {

        // 1. KONTEN HALAMAN (SWIPEABLE / GESER)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            // Tambahkan padding bawah agar konten terbawah tidak tertutup Menu Melayang
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 100.dp)) {
                when (page) {
                    0 -> {
                        HabitsScreen(
                            habits = appData?.habits ?: emptyList(),
                            onHabitCheckedChanged = { habit, isChecked ->
                                viewModel.toggleHabitCompleted(habit.id, isChecked)
                            },
                            onEditClick = { habit ->
                                habitToEdit = habit
                                showManualAddSheet = true
                            },
                            onDeleteClick = { habit -> habitToDelete = habit }
                        )
                    }
                    1 -> {
                        val (xpProgress, max) = viewModel.getXpProgress()
                        StatsScreen(
                            level = appData?.level ?: 1,
                            streak = appData?.streak ?: 0,
                            totalXp = appData?.totalXp ?: 0,
                            xpProgress = xpProgress,
                            maxXp = max,
                            userName = appData?.userName ?: "Petualang",
                            userTitle = appData?.userTitle ?: "Baru",
                            profileImageResId = viewModel.profileImageResId.collectAsStateWithLifecycle().value,
                            onScheduleReminderClick = onScheduleReminderClick,
                            onNameClick = { showNameEditDialog = true },
                            onAvatarClick = { showAvatarPickerSheet = true },
                            onTitleClick = { showTitlePickerSheet = true },
                            viewModel = viewModel // Passing viewModel untuk History Log
                        )
                    }
                    2 -> {
                        AchievementsScreen(
                            achievements = viewModel.achievements.collectAsStateWithLifecycle().value
                        )
                    }
                }
            }
        }

        // 2. FLOATING ACTION BUTTON (Hanya muncul di Halaman Habits / Page 0)
        // Kita letakkan manual di Box agar posisinya pas di atas Glass Bar
        AnimatedVisibility(
            visible = pagerState.currentPage == 0,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 110.dp, end = 24.dp) // Jarak dari bawah disesuaikan agar tidak menabrak menu
        ) {
            FloatingActionButton(
                onClick = { showAddOptionsSheet = true },
                containerColor = PrimaryColor,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah")
            }
        }

        // 3. GLASS BOTTOM NAVIGATION (Melayang di Bawah)
        GlassBottomNavigation(
            selectedIndex = pagerState.currentPage,
            onItemSelected = { index ->
                scope.launch {
                    pagerState.animateScrollToPage(index)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 24.dp, end = 24.dp) // Mengambang (Floating)
        )
    }

    // --- LOGIKA BOTTOM SHEETS & DIALOGS ---

    // Dialog Edit Nama
    if (showNameEditDialog) {
        val userName by viewModel.userName.collectAsStateWithLifecycle()
        EditNameDialog(
            currentName = userName,
            onDismiss = { showNameEditDialog = false },
            onConfirm = { newName -> viewModel.updateUserName(newName) }
        )
    }

    // Bottom Sheet Pilih Avatar
    if (showAvatarPickerSheet) {
        val avatarList by viewModel.avatarListWithLockStatus.collectAsStateWithLifecycle()
        ModalBottomSheet(
            onDismissRequest = { showAvatarPickerSheet = false },
            containerColor = DarkBackground
        ) {
            AvatarPickerSheet(
                avatarList = avatarList,
                currentAvatarId = appData?.profileImageId ?: "avatar_level1",
                onAvatarSelected = { avatarId ->
                    viewModel.updateProfileImageId(avatarId)
                    scope.launch { showAvatarPickerSheet = false }
                }
            )
        }
    }

    // Bottom Sheet Pilih Gelar
    if (showTitlePickerSheet) {
        val titleList by viewModel.titleListWithLockStatus.collectAsStateWithLifecycle()
        val currentTitle by viewModel.userTitle.collectAsStateWithLifecycle()
        ModalBottomSheet(
            onDismissRequest = { showTitlePickerSheet = false },
            containerColor = DarkBackground
        ) {
            TitlePickerSheet(
                titleList = titleList,
                currentTitle = currentTitle,
                onTitleSelected = { newTitle ->
                    viewModel.updateUserTitle(newTitle)
                    scope.launch { showTitlePickerSheet = false }
                }
            )
        }
    }

    // Add Options Sheet
    if (showAddOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddOptionsSheet = false },
            containerColor = CardBackground
        ) {
            AddOptionsSheet(
                onManualAddClick = {
                    scope.launch {
                        showAddOptionsSheet = false
                        habitToEdit = null
                        showManualAddSheet = true
                    }
                },
                onTemplateAddClick = {
                    scope.launch {
                        showAddOptionsSheet = false
                        showTemplateSheet = true
                    }
                }
            )
        }
    }

    // Manual Add Sheet
    if (showManualAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showManualAddSheet = false },
            containerColor = CardBackground
        ) {
            ManualAddHabitSheet(
                habitToEdit = habitToEdit,
                onConfirm = { name, schedule, weight ->
                    scope.launch {
                        if (habitToEdit == null) {
                            viewModel.addHabit(name, schedule, weight)
                        } else {
                            viewModel.updateHabit(habitToEdit!!.id, name, schedule, weight)
                        }
                        showManualAddSheet = false
                    }
                },
                onCancel = { scope.launch { showManualAddSheet = false } }
            )
        }
    }

    // Template Sheet
    if (showTemplateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTemplateSheet = false },
            containerColor = DarkBackground
        ) {
            TemplateHabitSheet(
                templates = predefinedHabitTemplates,
                onTemplateClick = { template ->
                    scope.launch { viewModel.addHabit(template.name, template.schedule, template.weight) }
                    Toast.makeText(context, "${template.name} ditambahkan!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // Delete Dialog
    if (habitToDelete != null) {
        DeleteConfirmationDialog(
            habitName = habitToDelete!!.name,
            onConfirm = {
                viewModel.deleteHabit(habitToDelete!!.id)
                habitToDelete = null
            },
            onDismiss = { habitToDelete = null }
        )
    }
}

// ─── KOMPONEN CUSTOM: GLASS BOTTOM NAVIGATION (MENU IPHONE STYLE) ───
@Composable
fun GlassBottomNavigation(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple(0, Icons.Filled.List, "Habits"),
        Triple(1, Icons.Filled.BarChart, "Stats"),
        Triple(2, Icons.Filled.EmojiEvents, "Prestasi")
    )

    // Container Glass Effect
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp) // Tinggi Bar
            // 1. Clip bentuk kapsul/rounded penuh
            .clip(RoundedCornerShape(35.dp))
            // 2. Background semi-transparan gelap (Glass)
            .background(Color(0xFF252525).copy(alpha = 0.85f))
            // 3. Border gradasi tipis (efek mengkilap)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f), // Atas lebih terang
                        Color.White.copy(alpha = 0.05f)  // Bawah gelap
                    )
                ),
                shape = RoundedCornerShape(35.dp)
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (index, icon, label) ->
                val isSelected = selectedIndex == index

                // Animasi warna ikon saat dipilih
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) PrimaryColor else TextColorSecondary.copy(alpha = 0.6f),
                    animationSpec = tween(300),
                    label = "iconColor"
                )

                // Item Navigasi
                Column(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // Matikan ripple standar agar lebih clean
                        ) { onItemSelected(index) }
                        .padding(12.dp), // Area sentuh
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = iconColor,
                        modifier = Modifier.size(26.dp)
                    )

                    // Indikator Titik Kecil (Hanya muncul jika dipilih)
                    AnimatedVisibility(
                        visible = isSelected,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(4.dp)
                                .background(PrimaryColor, CircleShape)
                        )
                    }
                }
            }
        }
    }
}