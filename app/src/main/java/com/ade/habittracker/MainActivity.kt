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
import androidx.compose.foundation.lazy.itemsIndexed // Import untuk leaderboard
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
// Import Leaderboard icon
import androidx.compose.material.icons.filled.Leaderboard // Nama ikon bisa berbeda
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
import com.ade.habittracker.data.UserData // Import UserData untuk leaderboard
import com.ade.habittracker.model.Achievement
import com.ade.habittracker.model.Habit // Pastikan Habit punya firestoreId: String
import com.ade.habittracker.ui.theme.HabitTrackerTheme
import com.ade.habittracker.ui.viewmodel.HabitViewModel
import kotlinx.coroutines.launch

// Definisi Warna (asumsi sama)
val DarkBackground = Color(0xFF2C2C2E)
val CardBackground = Color(0xFF3A3A3C)
val PrimaryColor = Color(0xFF0A84FF)
val TextColorPrimary = Color(0xFFFFFFFF)
val TextColorSecondary = Color(0xFF8E8E93)
val AccentYellow = Color(0xFFFFCC00)
val ErrorColor = Color(0xFFFF453A)

class MainActivity : ComponentActivity() {
    private val viewModel: HabitViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                viewModel.scheduleDailyReminder(applicationContext)
                Toast.makeText(this, "Pengingat harian diaktifkan!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Izin notifikasi ditolak.", Toast.LENGTH_SHORT).show()
            }
        }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
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
    // Panggil fungsi reset saat aplikasi pertama kali dibuka
    LaunchedEffect(Unit) {
        viewModel.resetHabitsIfNewDay()
    }

    val navController = rememberNavController()
    var showBottomSheet by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) } // Tetap pakai model Habit UI
    var habitToDelete by remember { mutableStateOf<Habit?>(null) } // Tetap pakai model Habit UI

    Scaffold(
        containerColor = DarkBackground,
        // --- PERUBAHAN DI SINI: Tambahkan item Leaderboard ---
        bottomBar = { BottomNavigationBar(navController = navController) },
        floatingActionButton = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute == "habits") {
                FloatingActionButton(
                    onClick = {
                        habitToEdit = null // Mode tambah baru
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
            onEditClick = { habit -> // Terima objek Habit dari HabitsScreen
                habitToEdit = habit
                showBottomSheet = true
            },
            onDeleteClick = { habit -> // Terima objek Habit dari HabitsScreen
                habitToDelete = habit
            },
            onScheduleReminderClick = onScheduleReminderClick
        )
    }

    if (showBottomSheet) {
        val scope = rememberCoroutineScope() // Scope untuk menutup bottom sheet
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = CardBackground
        ) {
            AddHabitBottomSheetContent(
                habitToEdit = habitToEdit,
                onConfirm = { name, schedule, weight ->
                    // Gunakan scope.launch di sini karena onConfirm adalah suspend
                    scope.launch {
                        if (habitToEdit == null) {
                            viewModel.addHabit(name, schedule, weight)
                        } else {
                            // --- PERUBAHAN DI SINI: Gunakan firestoreId ---
                            viewModel.updateHabit(habitToEdit!!.firestoreId, name, schedule, weight)
                        }
                        showBottomSheet = false // Tutup setelah selesai
                    }
                },
                onCancel = {
                    // Gunakan scope.launch di sini karena onCancel adalah suspend
                    scope.launch {
                        showBottomSheet = false // Cukup tutup
                    }
                }
            )
        }
    }

    if (habitToDelete != null) {
        DeleteConfirmationDialog(
            habitName = habitToDelete!!.name,
            onConfirm = {
                // --- PERUBAHAN DI SINI: Gunakan firestoreId ---
                viewModel.deleteHabit(habitToDelete!!.firestoreId)
                habitToDelete = null // Reset state setelah konfirmasi
            },
            onDismiss = {
                habitToDelete = null // Reset state saat batal
            }
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: HabitViewModel,
    onEditClick: (Habit) -> Unit, // Terima objek Habit
    onDeleteClick: (Habit) -> Unit, // Terima objek Habit
    onScheduleReminderClick: () -> Unit
) {
    val appData by viewModel.appData.collectAsStateWithLifecycle()
    // Ambil data leaderboard dari ViewModel
    val leaderboard by viewModel.leaderboardData.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "habits", // Tetap mulai dari habits
        modifier = modifier
    ) {
        composable("habits") {
            HabitsScreen(
                habits = appData?.habits ?: emptyList(),
                onHabitCheckedChanged = { habit, isChecked -> // Terima objek Habit
                    // --- PERUBAHAN DI SINI: Gunakan firestoreId ---
                    viewModel.toggleHabitCompleted(habit.firestoreId, isChecked)
                },
                onEditClick = onEditClick, // Teruskan lambda onEditClick
                onDeleteClick = onDeleteClick // Teruskan lambda onDeleteClick
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
        // --- PERUBAHAN DI SINI: Tambahkan route Leaderboard ---
        composable("leaderboard") {
            LeaderboardScreen(leaderboardData = leaderboard)
        }
    }
}

@Composable
fun HabitsScreen(
    habits: List<Habit>,
    onHabitCheckedChanged: (Habit, Boolean) -> Unit, // Terima objek Habit
    onEditClick: (Habit) -> Unit, // Terima objek Habit
    onDeleteClick: (Habit) -> Unit // Terima objek Habit
) {
    // --- PERUBAHAN DI SINI: Gunakan String ID ---
    var expandedMenuHabitId by remember { mutableStateOf<String?>(null) }

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
                Text("Memuat data atau belum ada misi...", color = TextColorSecondary) // Ubah teks
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // --- PERUBAHAN DI SINI: Gunakan firestoreId sebagai key ---
                items(habits, key = { it.firestoreId }) { habit ->
                    HabitItem(
                        habit = habit,
                        onCheckedChanged = { isChecked ->
                            // Kirim objek Habit ke atas
                            onHabitCheckedChanged(habit, isChecked)
                        },
                        // --- PERUBAHAN DI SINI: Bandingkan firestoreId ---
                        isMenuExpanded = expandedMenuHabitId == habit.firestoreId,
                        onMenuClick = {
                            expandedMenuHabitId = if (expandedMenuHabitId == habit.firestoreId) null else habit.firestoreId
                        },
                        onDismissMenu = { expandedMenuHabitId = null },
                        // Kirim objek Habit ke atas
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
    onEditClick: () -> Unit, // Lambda tidak perlu parameter
    onDeleteClick: () -> Unit // Lambda tidak perlu parameter
) {
    val borderColor = if (habit.isCompleted) PrimaryColor else CardBackground
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(2.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize() // Animasi saat ukuran berubah
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
                fontSize = 16.sp,
                textAlign = TextAlign.Center // Rata tengah XP
            )
            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = TextColorSecondary)
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = onDismissMenu,
                    modifier = Modifier.background(Color(0xFF48484A)) // Warna background menu
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit", color = TextColorPrimary) },
                        onClick = {
                            onEditClick() // Panggil lambda onEditClick
                            onDismissMenu()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus", color = Color.Red) },
                        onClick = {
                            onDeleteClick() // Panggil lambda onDeleteClick
                            onDismissMenu()
                        }
                    )
                }
            }
        }
    }
}


// Composable StatsScreen, StatCard, AchievementsScreen, AchievementItem TETAP SAMA
@Composable
fun StatsScreen(level: Int, streak: Int, totalXp: Int, xpProgress: Int, maxXp: Int, onScheduleReminderClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text( /* ... Judul ... */)
        Spacer(modifier = Modifier.height(32.dp)) // Beri jarak lebih
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            CircularProgressIndicator(
                // Pastikan lambda progress benar dan aman dari pembagian nol
                progress = { (xpProgress.toFloat() / maxXp.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 12.dp,
                color = AccentYellow,
                trackColor = CardBackground
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) { /* ... Text Level & XP ... */ }
        }
        Spacer(modifier = Modifier.height(48.dp)) // Beri jarak lebih
        Row( /* ... StatCard Streak & Total XP ... */)
        Spacer(modifier = Modifier.weight(1f)) // Dorong tombol ke bawah
        Button( /* ... Tombol Notifikasi ... */)
        Text( /* ... Teks Bantuan Notifikasi ... */)
    }
}
@Composable fun StatCard(/* ... */) { /* ... */ }
@Composable fun AchievementsScreen(/* ... */) { /* ... */ }
@Composable fun AchievementItem(/* ... */) { /* ... */ }


@Composable
fun BottomNavigationBar(navController: NavController) {
    // --- PERUBAHAN DI SINI: Tambahkan item Leaderboard ---
    val items = listOf(
        NavigationItem("habits", Icons.Default.List, "Habits"),
        NavigationItem("stats", Icons.Default.BarChart, "Statistik"),
        // Icon Leaderboard mungkin perlu diganti
        NavigationItem("leaderboard", Icons.Default.Leaderboard, "Peringkat"),
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
                    indicatorColor = CardBackground // Warna indicator saat item dipilih
                )
            )
        }
    }
}

// Data class NavigationItem tetap sama
data class NavigationItem(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitBottomSheetContent(
    habitToEdit: Habit?,
    onConfirm: suspend (String, String, Int) -> Unit, // Tetap suspend
    onCancel: suspend () -> Unit // Tetap suspend
) {
    var name by remember { mutableStateOf("") }
    var schedule by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Mengisi form jika dalam mode edit
    LaunchedEffect(habitToEdit) {
        if (habitToEdit != null) {
            name = habitToEdit.name
            schedule = habitToEdit.schedule
            weight = habitToEdit.weight.toString()
        } else {
            // Reset form jika mode tambah baru (penting saat bottom sheet dibuka lagi)
            name = ""
            schedule = ""
            weight = ""
        }
    }

    // Validasi input
    val weightValue = weight.toIntOrNull()
    val isWeightError = weight.isNotEmpty() && (weightValue == null || weightValue !in 1..100)
    val isFormValid = name.isNotBlank() &&
            schedule.isNotBlank() &&
            weight.isNotEmpty() &&
            !isWeightError

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            // Padding untuk sistem navigasi gestur / tombol bawah
            .navigationBarsPadding()
            // Padding untuk keyboard saat muncul
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (habitToEdit == null) "Tambah Misi Baru" else "Edit Misi",
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextColorPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = name, onValueChange = { name = it }, label = { Text("Nama Misi") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = TextFieldDefaults.colors(/* ... */)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = schedule, onValueChange = { schedule = it }, label = { Text("Jadwal (cth: Setiap Hari)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = TextFieldDefaults.colors(/* ... */)
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
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = isWeightError,
            supportingText = {
                if (isWeightError) {
                    Text("XP harus antara 1-100", color = ErrorColor)
                } else {
                    Text("Masukkan bobot 1-100", color = TextColorSecondary)
                }
            },
            colors = TextFieldDefaults.colors(/* ... Warna normal & error ... */)
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { scope.launch { onCancel() } }, colors = ButtonDefaults.buttonColors(containerColor = TextColorSecondary)) { Text("Batal") }
            Button(
                onClick = { weightValue?.let { scope.launch { onConfirm(name, schedule, it) } } },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) { Text(if (habitToEdit == null) "Tambah Misi" else "Simpan Perubahan") }
        }
        // Spacer bawah tidak perlu jika pakai imePadding/navigationBarsPadding
        // Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun DeleteConfirmationDialog(
    habitName: String,
    onConfirm: () -> Unit, // Tetap biasa
    onDismiss: () -> Unit  // Tetap biasa
) {
    // Kode DeleteConfirmationDialog tetap sama
    AlertDialog(/* ... */)
}


// --- COMPOSABLE BARU UNTUK LEADERBOARD ---
@Composable
fun LeaderboardScreen(leaderboardData: List<UserData>) { // Terima data leaderboard
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "PAPAN PERINGKAT MINGGUAN",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextColorPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (leaderboardData.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Tampilkan CircularProgressIndicator saat loading
                CircularProgressIndicator(color = PrimaryColor)
                // Text("Memuat data peringkat...", color = TextColorSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(leaderboardData) { index, user ->
                    LeaderboardItem(rank = index + 1, user = user)
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(rank: Int, user: UserData) { // Terima UserData
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp), // Sedikit lebih bulat
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (rank <= 3) AccentYellow else TextColorPrimary, // Sorot top 3
                modifier = Modifier.width(40.dp) // Beri lebar agar rata
            )
            Column(modifier = Modifier.weight(1f)) {
                // Tampilkan email sebagai identifier utama
                Text(
                    text = user.email ?: "Pengguna Anonim",
                    fontSize = 16.sp,
                    color = TextColorPrimary,
                    maxLines = 1 // Batasi jika email panjang
                )
            }
            Text(
                text = "${user.weeklyXp} XP", // Gunakan weeklyXp
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AccentYellow
            )
        }
    }
}

