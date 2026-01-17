package com.ade.habittracker.ui.navigation

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary
import com.ade.habittracker.ui.viewmodel.HabitViewModel
import kotlinx.coroutines.delay
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

    val pagerState = rememberPagerState(pageCount = { 3 })

    // --- STATE MANAGEMENT ---
    var showAddOptionsSheet by remember { mutableStateOf(false) }
    var showManualAddSheet by remember { mutableStateOf(false) }
    var showTemplateSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }
    var showNameEditDialog by remember { mutableStateOf(false) }
    var showAvatarPickerSheet by remember { mutableStateOf(false) }
    var showTitlePickerSheet by remember { mutableStateOf(false) }

    // --- IDLE DETECTION ---
    var isUserIdle by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(lastInteractionTime) {
        isUserIdle = false
        delay(3000)
        isUserIdle = true
    }

    // --- CONTAINER UTAMA ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    lastInteractionTime = System.currentTimeMillis()
                    waitForUpOrCancellation()
                }
            }
    ) {

        // 1. HORIZONTAL PAGER (SWIPE LAYAR)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(bottom = 100.dp)
            ) {
                when (page) {
                    0 -> {
                        HabitsScreen(
                            habits = appData?.habits ?: emptyList(),
                            // 🔥 Pass Status Chibi (Nyala/Mati)
                            isChibiEnabled = appData?.isChibiEnabled ?: true,
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
                            totalLoginDays = appData?.totalLoginDays ?: 1,
                            userName = appData?.userName ?: "Petualang",
                            userTitle = appData?.userTitle ?: "Baru",
                            profileImageResId = viewModel.profileImageResId.collectAsStateWithLifecycle().value,
                            onScheduleReminderClick = onScheduleReminderClick,
                            onNameClick = { showNameEditDialog = true },
                            onAvatarClick = { showAvatarPickerSheet = true },
                            onTitleClick = { showTitlePickerSheet = true },
                            viewModel = viewModel
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

        // 2. TOMBOL FLOAT (SETTINGS & ADD)
        val fabScale by animateFloatAsState(if (isUserIdle) 0f else 1f, label = "fabScale")

        AnimatedVisibility(
            visible = pagerState.currentPage == 0,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 90.dp, end = 20.dp)
                .scale(fabScale)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // 🔥 Tombol Pengaturan (Besar) 🔥
                FloatingActionButton(
                    onClick = { showSettingsSheet = true },
                    containerColor = CardBackground,
                    contentColor = TextColorPrimary,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Filled.Settings, "Set")
                }

                // Tombol Tambah (+)
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
        }

        // 3. MENU NAVIGASI GLASS
        GlassBottomNavigation(
            selectedIndex = pagerState.currentPage,
            isIdle = isUserIdle,
            onItemSelected = { index ->
                scope.launch { pagerState.animateScrollToPage(index) }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 10.dp, start = 20.dp, end = 20.dp)
        )
    }

    // --- MODAL SHEETS & DIALOGS ---

    // 1. Settings Sheet (Musik & Chibi Toggle)
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            containerColor = DarkBackground
        ) {
            SettingsSheet(
                isMusicEnabled = appData?.isMusicEnabled ?: true,
                onMusicToggle = { viewModel.setMusicEnabled(it) },
                // 🔥 Parameter Chibi Toggle
                isChibiEnabled = appData?.isChibiEnabled ?: true,
                onChibiToggle = { viewModel.setChibiEnabled(it) }
            )
        }
    }

    // 2. Dialog Edit Nama
    if (showNameEditDialog) {
        val userName by viewModel.userName.collectAsStateWithLifecycle()
        EditNameDialog(userName, { showNameEditDialog = false }) { viewModel.updateUserName(it) }
    }

    // 3. Avatar Picker
    if (showAvatarPickerSheet) {
        val avatarList by viewModel.avatarListWithLockStatus.collectAsStateWithLifecycle()
        ModalBottomSheet(onDismissRequest = { showAvatarPickerSheet = false }, containerColor = DarkBackground) {
            AvatarPickerSheet(avatarList, appData?.profileImageId ?: "avatar_level1") {
                viewModel.updateProfileImageId(it)
                scope.launch { showAvatarPickerSheet = false }
            }
        }
    }

    // 4. Title Picker
    if (showTitlePickerSheet) {
        val titleList by viewModel.titleListWithLockStatus.collectAsStateWithLifecycle()
        val currentTitle by viewModel.userTitle.collectAsStateWithLifecycle()
        ModalBottomSheet(onDismissRequest = { showTitlePickerSheet = false }, containerColor = DarkBackground) {
            TitlePickerSheet(titleList, currentTitle) {
                viewModel.updateUserTitle(it)
                scope.launch { showTitlePickerSheet = false }
            }
        }
    }

    // 5. Add Options
    if (showAddOptionsSheet) {
        ModalBottomSheet(onDismissRequest = { showAddOptionsSheet = false }, containerColor = CardBackground) {
            AddOptionsSheet(
                onManualAddClick = { scope.launch { showAddOptionsSheet = false; habitToEdit = null; showManualAddSheet = true } },
                onTemplateAddClick = { scope.launch { showAddOptionsSheet = false; showTemplateSheet = true } }
            )
        }
    }

    // 6. Manual Add/Edit
    if (showManualAddSheet) {
        ModalBottomSheet(onDismissRequest = { showManualAddSheet = false }, containerColor = CardBackground) {
            ManualAddHabitSheet(habitToEdit, { name, sched, w ->
                scope.launch {
                    if (habitToEdit == null) viewModel.addHabit(name, sched, w)
                    else viewModel.updateHabit(habitToEdit!!.id, name, sched, w)
                    showManualAddSheet = false
                }
            }, { scope.launch { showManualAddSheet = false } })
        }
    }

    // 7. Template Add
    if (showTemplateSheet) {
        ModalBottomSheet(onDismissRequest = { showTemplateSheet = false }, containerColor = DarkBackground) {
            TemplateHabitSheet(predefinedHabitTemplates) { t ->
                scope.launch { viewModel.addHabit(t.name, t.schedule, t.weight) }
                Toast.makeText(context, "${t.name} ditambahkan!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 8. Delete Dialog
    if (habitToDelete != null) {
        DeleteConfirmationDialog(habitToDelete!!.name, {
            viewModel.deleteHabit(habitToDelete!!.id)
            habitToDelete = null
        }, { habitToDelete = null })
    }
}

// ─── SETTINGS SHEET (SEDERHANA: MUSIK & CHIBI) ───
@Composable
fun SettingsSheet(
    isMusicEnabled: Boolean,
    onMusicToggle: (Boolean) -> Unit,
    isChibiEnabled: Boolean,
    onChibiToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text("Pengaturan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextColorPrimary)
        Spacer(Modifier.height(24.dp))

        // 1. Musik Latar Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Musik Latar", color = TextColorSecondary, fontSize = 16.sp)
            Switch(
                checked = isMusicEnabled,
                onCheckedChange = onMusicToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PrimaryColor,
                    checkedTrackColor = PrimaryColor.copy(alpha = 0.3f)
                )
            )
        }

        Divider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 16.dp))

        // 2. Tampilkan Chibi Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tampilkan Asisten Chibi", color = TextColorSecondary, fontSize = 16.sp)
            Switch(
                checked = isChibiEnabled,
                onCheckedChange = onChibiToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PrimaryColor,
                    checkedTrackColor = PrimaryColor.copy(alpha = 0.3f)
                )
            )
        }

        // Splash Screen Settings sudah dihapus total
    }
}

// ─── CUSTOM CHIP (Backup jika butuh) ───
@Composable
fun CustomSettingsChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (selected) PrimaryColor else CardBackground,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (selected) PrimaryColor else TextColorSecondary.copy(alpha = 0.5f)),
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else TextColorSecondary
            )
        }
    }
}

// ─── GLASS MENU ───
@Composable
fun GlassBottomNavigation(
    selectedIndex: Int,
    isIdle: Boolean,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple(0, Icons.Filled.List, "Habits"),
        Triple(1, Icons.Filled.BarChart, "Stats"),
        Triple(2, Icons.Filled.EmojiEvents, "Prestasi")
    )

    // Animasi
    val animatedScale by animateFloatAsState(if (isIdle) 0.85f else 1f, tween(500), label = "scale")
    val animatedAlpha by animateFloatAsState(if (isIdle) 0.4f else 0.95f, tween(500), label = "alpha")
    val animatedWidthFraction by animateFloatAsState(if (isIdle) 0.6f else 1f, tween(500), label = "width")

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedWidthFraction)
                .scale(animatedScale)
                .alpha(animatedAlpha)
                .height(70.dp)
                .clip(RoundedCornerShape(35.dp))
                .background(Color(0xFF252525).copy(alpha = 0.90f))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))
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
                    val iconColor by animateColorAsState(
                        if (isSelected) PrimaryColor else TextColorSecondary.copy(alpha = 0.6f),
                        tween(300), label = "iconColor"
                    )

                    Column(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onItemSelected(index) }
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(icon, label, tint = iconColor, modifier = Modifier.size(26.dp))
                        AnimatedVisibility(visible = isSelected && !isIdle, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
                            Box(modifier = Modifier.padding(top = 4.dp).size(4.dp).background(PrimaryColor, CircleShape))
                        }
                    }
                }
            }
        }
    }
}