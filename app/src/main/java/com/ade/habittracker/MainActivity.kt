package com.ade.habittracker

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ade.habittracker.model.Achievement
import com.ade.habittracker.model.Habit
import com.ade.habittracker.ui.theme.HabitTrackerTheme
import com.ade.habittracker.ui.viewmodel.HabitViewModel
import kotlinx.coroutines.launch

// Definisi Warna
val DarkBackground = Color(0xFF2C2C2E)
val CardBackground = Color(0xFF3A3A3C)
val PrimaryColor = Color(0xFF0A84FF) // Biru cerah
val TextColorPrimary = Color(0xFFFFFFFF)
val TextColorSecondary = Color(0xFF8E8E93)
val AccentYellow = Color(0xFFFFCC00)
val ErrorColor = Color(0xFFFF453A)

class MainActivity : ComponentActivity() {
    private val viewModel: HabitViewModel by viewModels()

    // Logic untuk meminta izin notifikasi
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Izin diberikan, jadwalkan notifikasi
                viewModel.scheduleDailyReminder(applicationContext)
                Toast.makeText(this, "Pengingat harian diaktifkan!", Toast.LENGTH_SHORT).show()
            } else {
                // Izin ditolak
                Toast.makeText(this, "Izin notifikasi ditolak.", Toast.LENGTH_SHORT).show()
            }
        }

    private fun askNotificationPermission() {
        // Hanya perlu untuk Android 13 (TIRAMISU) ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Untuk versi Android lebih lama, izin sudah ada secara default
            viewModel.scheduleDailyReminder(applicationContext)
            Toast.makeText(this, "Pengingat harian diaktifkan!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitTrackerTheme {
                MainScreen(
                    viewModel = viewModel,
                    onScheduleReminderClick = {
                        askNotificationPermission()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: HabitViewModel,
    onScheduleReminderClick: () -> Unit
) {
    // --- DI SINI PERUBAHANNYA ---
    // LaunchedEffect akan menjalankan blok kode ini satu kali
    // saat MainScreen pertama kali ditampilkan/dibuka.
    LaunchedEffect(Unit) {
        viewModel.resetHabitsIfNewDay()
    }
    // ---------------------------

    val navController = rememberNavController()
    var showBottomSheet by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = { BottomNavigationBar(navController = navController) },
        floatingActionButton = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute == "habits") {
                FloatingActionButton(
                    onClick = {
                        habitToEdit = null // Pastikan mode tambah baru, bukan edit
                        showBottomSheet = true
                    },
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
                showBottomSheet = true
            },
            onDeleteClick = { habit ->
                habitToDelete = habit
            },
            onScheduleReminderClick = onScheduleReminderClick
        )
    }

    if (showBottomSheet) {
        val scope = rememberCoroutineScope()
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = CardBackground
        ) {
            AddHabitBottomSheetContent(
                habitToEdit = habitToEdit,
                onConfirm = { name, schedule, weight ->
                    scope.launch {
                        if (habitToEdit == null) {
                            viewModel.addHabit(name, schedule, weight)
                        } else {
                            viewModel.updateHabit(habitToEdit!!.id, name, schedule, weight)
                        }
                        showBottomSheet = false
                    }
                },
                onCancel = {
                    scope.launch { showBottomSheet = false }
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
    onScheduleReminderClick: () -> Unit
) {
    val appData by viewModel.appData.collectAsStateWithLifecycle()

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
                onScheduleReminderClick = onScheduleReminderClick
            )
        }
        composable("achievements") {
            AchievementsScreen(achievements = appData?.achievements ?: emptyList())
        }
    }
}

@Composable
fun HabitsScreen(
    habits: List<Habit>,
    onHabitCheckedChanged: (Habit, Boolean) -> Unit,
    onEditClick: (Habit) -> Unit,
    onDeleteClick: (Habit) -> Unit
) {
    var expandedMenuHabitId by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "DAFTAR MISI HARIAN",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextColorPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (habits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada misi. Tambahkan satu!", color = TextColorSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(habits, key = { it.id }) { habit ->
                    HabitItem(
                        habit = habit,
                        onCheckedChanged = { isChecked ->
                            onHabitCheckedChanged(habit, isChecked)
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
}

@Composable
fun HabitItem(
    habit: Habit,
    onCheckedChanged: (Boolean) -> Unit,
    isMenuExpanded: Boolean,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val borderColor = if (habit.isCompleted) PrimaryColor else CardBackground
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(2.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = habit.isCompleted,
                onCheckedChange = onCheckedChanged,
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryColor,
                    uncheckedColor = TextColorSecondary,
                    checkmarkColor = Color.White
                )
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(habit.name, color = TextColorPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(habit.schedule, color = TextColorSecondary, fontSize = 14.sp)
            }
            Spacer(Modifier.width(16.dp))
            Text(
                "+${habit.weight}\nXP",
                color = AccentYellow,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp,
                fontSize = 16.sp
            )
            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = TextColorSecondary)
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = onDismissMenu,
                    modifier = Modifier.background(Color(0xFF48484A))
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit", color = TextColorPrimary) },
                        onClick = {
                            onEditClick()
                            onDismissMenu()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus", color = Color.Red) },
                        onClick = {
                            onDeleteClick()
                            onDismissMenu()
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun StatsScreen(
    level: Int,
    streak: Int,
    totalXp: Int,
    xpProgress: Int,
    maxXp: Int,
    onScheduleReminderClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "PROFIL & STATS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextColorPrimary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            CircularProgressIndicator(
                progress = { (xpProgress.toFloat() / maxXp.toFloat()) },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 12.dp,
                color = AccentYellow,
                trackColor = CardBackground
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LEVEL", color = TextColorSecondary, fontSize = 16.sp)
                Text(
                    text = "$level",
                    color = TextColorPrimary,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("$xpProgress/$maxXp XP", color = TextColorSecondary, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(title = "STREAK", value = "$streak HARI", icon = Icons.Default.CheckCircle, iconColor = Color.Red)
            StatCard(title = "TOTAL XP", value = "$totalXp", icon = Icons.Default.Star, iconColor = AccentYellow)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onScheduleReminderClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, PrimaryColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifikasi", tint = PrimaryColor)
                Spacer(Modifier.width(8.dp))
                Text("Aktifkan Pengingat Harian", color = PrimaryColor)
            }
        }
        Text(
            "Anda akan diingatkan setiap hari.",
            color = TextColorSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        modifier = Modifier.size(width = 160.dp, height = 120.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = TextColorPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(title, color = TextColorSecondary, fontSize = 14.sp)
        }
    }
}


@Composable
fun AchievementsScreen(achievements: List<Achievement>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "PENCAPAIAN",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextColorPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (achievements.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada pencapaian tersedia.", color = TextColorSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(achievements) { achievement ->
                    AchievementItem(achievement = achievement)
                }
            }
        }
    }
}

@Composable
fun AchievementItem(achievement: Achievement) {
    val borderColor = if (achievement.isUnlocked) AccentYellow else CardBackground
    val iconColor = if (achievement.isUnlocked) AccentYellow else TextColorSecondary

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF48484A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = "Icon Bintang", tint = iconColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(achievement.title, color = TextColorPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(achievement.description, color = TextColorSecondary, fontSize = 14.sp)
            }
            if (achievement.isUnlocked) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Tercapai",
                    tint = AccentYellow,
                    modifier = Modifier.size(24.dp)
                )
            }
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

data class NavigationItem(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitBottomSheetContent(
    habitToEdit: Habit?,
    onConfirm: (String, String, Int) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var schedule by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    LaunchedEffect(habitToEdit) {
        if (habitToEdit != null) {
            name = habitToEdit.name
            schedule = habitToEdit.schedule
            weight = habitToEdit.weight.toString()
        } else {
            // Pastikan form kosong saat mode tambah
            name = ""
            schedule = ""
            weight = ""
        }
    }

    val weightValue = weight.toIntOrNull()
    val isWeightError = weight.isNotEmpty() && (weightValue == null || weightValue !in 1..100)
    val isFormValid = name.isNotBlank() &&
            schedule.isNotBlank() &&
            weight.isNotEmpty() &&
            !isWeightError

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (habitToEdit == null) "Tambah Misi Baru" else "Edit Misi",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextColorPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = name,
            onValueChange = { newName -> name = newName },
            label = { Text("Nama Misi") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = PrimaryColor,
                unfocusedIndicatorColor = TextColorSecondary,
                cursorColor = PrimaryColor,
                focusedTextColor = TextColorPrimary,
                unfocusedTextColor = TextColorPrimary,
                focusedLabelColor = PrimaryColor,
                unfocusedLabelColor = TextColorSecondary
            )
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = schedule,
            onValueChange = { newSchedule -> schedule = newSchedule },
            label = { Text("Jadwal (cth: Setiap Hari, Senin)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = PrimaryColor,
                unfocusedIndicatorColor = TextColorSecondary,
                cursorColor = PrimaryColor,
                focusedTextColor = TextColorPrimary,
                unfocusedTextColor = TextColorPrimary,
                focusedLabelColor = PrimaryColor,
                unfocusedLabelColor = TextColorSecondary
            )
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = weight,
            onValueChange = { newValue ->
                val filteredValue = newValue.filter { it.isDigit() }
                if (filteredValue.isEmpty() || (filteredValue.toIntOrNull() ?: 0) <= 100) {
                    weight = filteredValue
                }
            },
            label = { Text("Bobot (XP 1-100)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = isWeightError,
            supportingText = {
                if (isWeightError) {
                    Text("XP harus antara 1-100", color = ErrorColor)
                } else {
                    Text("Masukkan bobot antara 1-100", color = TextColorSecondary)
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = PrimaryColor,
                unfocusedIndicatorColor = TextColorSecondary,
                cursorColor = PrimaryColor,
                focusedTextColor = TextColorPrimary,
                unfocusedTextColor = TextColorPrimary,
                focusedLabelColor = PrimaryColor,
                unfocusedLabelColor = TextColorSecondary,
                errorCursorColor = ErrorColor,
                errorIndicatorColor = ErrorColor,
                errorSupportingTextColor = ErrorColor,
                errorLabelColor = ErrorColor
            )
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = TextColorSecondary)
            ) {
                Text("Batal")
            }
            Button(
                onClick = { onConfirm(name, schedule, weight.toInt()) },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text(if (habitToEdit == null) "Tambah Misi" else "Simpan Perubahan")
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun DeleteConfirmationDialog(
    habitName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hapus Misi?", color = TextColorPrimary) },
        text = { Text("Apakah Anda yakin ingin menghapus misi \"$habitName\"?", color = TextColorSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Hapus", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = PrimaryColor)
            }
        },
        containerColor = CardBackground
    )
}

