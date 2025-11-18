package com.ade.habittracker.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
// import androidx.compose.runtime.LaunchedEffect // Dihapus karena sudah dipindah ke MainActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ade.habittracker.data.predefinedHabitTemplates
import com.ade.habittracker.model.Habit
import com.ade.habittracker.ui.components.DeleteConfirmationDialog
import com.ade.habittracker.ui.components.EditNameDialog
import com.ade.habittracker.ui.components.sheets.AddOptionsSheet
import com.ade.habittracker.ui.components.sheets.AvatarPickerSheet
import com.ade.habittracker.ui.components.sheets.ManualAddHabitSheet
import com.ade.habittracker.ui.components.sheets.TemplateHabitSheet
import com.ade.habittracker.ui.components.sheets.TitlePickerSheet
import com.ade.habittracker.ui.screens.AchievementsScreen
import com.ade.habittracker.ui.screens.HabitsScreen
import com.ade.habittracker.ui.screens.StatsScreen
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.DarkBackground
import com.ade.habittracker.ui.theme.PrimaryColor
import com.ade.habittracker.ui.theme.TextColorSecondary
import com.ade.habittracker.ui.viewmodel.HabitViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: HabitViewModel,
    onScheduleReminderClick: () -> Unit
) {
    // Pengecekan reset harian sudah dipindah ke MainActivity.kt (onStart)
    // LaunchedEffect(Unit) { ... } sudah dihapus dari sini.

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val appData by viewModel.appData.collectAsStateWithLifecycle()

    // State untuk Bottom Sheet
    var showAddOptionsSheet by remember { mutableStateOf(false) }
    var showManualAddSheet by remember { mutableStateOf(false) }
    var showTemplateSheet by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    // State untuk Edit Profil
    var showNameEditDialog by remember { mutableStateOf(false) }
    var showAvatarPickerSheet by remember { mutableStateOf(false) }
    var showTitlePickerSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = { BottomNavigationBar(navController = navController) },
        floatingActionButton = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute == "habits") {
                FloatingActionButton(
                    onClick = { showAddOptionsSheet = true },
                    containerColor = PrimaryColor,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Tambah Habit")
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            viewModel = viewModel,
            onEditClick = { habit ->
                habitToEdit = habit
                showManualAddSheet = true
            },
            onDeleteClick = { habit ->
                habitToDelete = habit
            },
            onScheduleReminderClick = onScheduleReminderClick,

            onNameClick = { showNameEditDialog = true },
            onAvatarClick = { showAvatarPickerSheet = true },
            onTitleClick = { showTitlePickerSheet = true }
        )
    }

    // Dialog Edit Nama
    if (showNameEditDialog) {
        val userName by viewModel.userName.collectAsStateWithLifecycle()
        EditNameDialog(
            currentName = userName,
            onDismiss = { showNameEditDialog = false },
            onConfirm = { newName ->
                viewModel.updateUserName(newName)
            }
        )
    }

    // Bottom Sheet Pilih Avatar
    if (showAvatarPickerSheet) {
        val avatarList by viewModel.avatarListWithLockStatus.collectAsStateWithLifecycle()
        ModalBottomSheet(
            onDismissRequest = { showAvatarPickerSheet = false },
            sheetState = rememberModalBottomSheetState(),
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
            sheetState = rememberModalBottomSheetState(),
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

    // (Kode untuk bottom sheet Add/Edit Habit)
    if (showAddOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddOptionsSheet = false },
            sheetState = rememberModalBottomSheetState(),
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
    if (showManualAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showManualAddSheet = false },
            sheetState = rememberModalBottomSheetState(),
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
                onCancel = {
                    scope.launch { showManualAddSheet = false }
                }
            )
        }
    }
    if (showTemplateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTemplateSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = DarkBackground
        ) {
            TemplateHabitSheet(
                templates = predefinedHabitTemplates,
                onTemplateClick = { template ->
                    scope.launch {
                        viewModel.addHabit(template.name, template.schedule, template.weight)
                    }
                    Toast.makeText(navController.context, "${template.name} ditambahkan!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    if (habitToDelete != null) {
        DeleteConfirmationDialog(
            habitName = habitToDelete!!.name,
            onConfirm = {
                viewModel.deleteHabit(habitToDelete!!.id)
                habitToDelete = null
            },
            onDismiss = {
                habitToDelete = null
            }
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: HabitViewModel,
    onEditClick: (Habit) -> Unit,
    onDeleteClick: (Habit) -> Unit,
    onScheduleReminderClick: () -> Unit,
    onNameClick: () -> Unit,
// --- PERBAIKAN DI SINI: Menghapus 'Two a,' ---
    onAvatarClick: () -> Unit,
    onTitleClick: () -> Unit
) {
    val appData by viewModel.appData.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userTitle by viewModel.userTitle.collectAsStateWithLifecycle()
    val profileImageResId by viewModel.profileImageResId.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "habits",
        modifier = modifier
    ) {
        composable("habits") {
            HabitsScreen(
                habits = appData?.habits ?: emptyList(),
                onHabitCheckedChanged = { habit, isChecked ->
                    viewModel.toggleHabitCompleted(habit.id, isChecked)
                },
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick
            )
        }
        composable("stats") {
            val (xpProgress, max) = viewModel.getXpProgress()
            StatsScreen(
                level = appData?.level ?: 1,
                streak = appData?.streak ?: 0,
                totalXp = appData?.totalXp ?: 0,
                xpProgress = xpProgress,
                maxXp = max,
                userName = userName,
                userTitle = userTitle,
                profileImageResId = profileImageResId,
                onScheduleReminderClick = onScheduleReminderClick,

                onNameClick = onNameClick,
                onAvatarClick = onAvatarClick,
                onTitleClick = onTitleClick
            )
        }
        composable("achievements") {
            AchievementsScreen(
                achievements = achievements
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        NavigationItem("habits", Icons.Default.List, "Habits"),
        NavigationItem("stats", Icons.Default.BarChart, "Statistik"),
        NavigationItem("achievements", Icons.Default.EmojiEvents, "Pencapaian")
    )
    NavigationBar(
        containerColor = CardBackground,
        contentColor = TextColorSecondary
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryColor,
                    unselectedIconColor = TextColorSecondary,
                    selectedTextColor = PrimaryColor,
                    unselectedTextColor = TextColorSecondary,
                    indicatorColor = CardBackground
                )
            )
        }
    }
}

data class NavigationItem(val route: String, val icon: ImageVector, val title: String)