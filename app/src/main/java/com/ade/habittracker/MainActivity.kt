package com.ade.habittracker

import android.Manifest
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.graphics.vector.ImageVector
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
// import com.ade.habittracker.model.Achievement (IMPORT INI DIHAPUS)
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

// --- MODEL DATA ACHIEVEMENT BARU ---
// Model data lama Anda (dari import) diganti dengan ini
// untuk mendukung progress dan ikon
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean = false,
    val progress: Int = 0, // Progress saat ini (cth: 5 hari streak)
    val goal: Int = 1      // Target (cth: 7 hari streak)
)
// ----------------------------------

// --- LOGIKA ACHIEVEMENT BARU ---
// Logika ini idealnya ada di dalam HabitViewModel Anda,
// tapi kita letakkan di sini untuk demonstrasi.
fun getAllAchievements(
    level: Int,
    streak: Int,
    totalXp: Int,
    totalHabitsCompleted: Int // Anda perlu melacak ini di ViewModel
): List<Achievement> {
    val allAchievements = mutableListOf<Achievement>()

    // 1. Level Achievements
    allAchievements.add(
        Achievement(
            id = "level_5",
            title = "Level 5 Tercapai",
            description = "Capai level 5.",
            icon = Icons.Default.Star,
            isUnlocked = level >= 5,
            progress = minOf(level, 5),
            goal = 5
        )
    )
    allAchievements.add(
        Achievement(
            id = "level_10",
            title = "Level 10 Tercapai",
            description = "Capai level 10.",
            icon = Icons.Default.Star,
            isUnlocked = level >= 10,
            progress = minOf(level, 10),
            goal = 10
        )
    )
    allAchievements.add(
        Achievement(
            id = "level_20",
            title = "Master",
            description = "Capai level 20.",
            icon = Icons.Default.Star,
            isUnlocked = level >= 20,
            progress = minOf(level, 20),
            goal = 20
        )
    )


    // 2. Streak Achievements
    allAchievements.add(
        Achievement(
            id = "streak_3",
            title = "Pemanasan",
            description = "Capai 3 hari streak.",
            icon = Icons.Default.CheckCircle,
            isUnlocked = streak >= 3,
            progress = minOf(streak, 3),
            goal = 3
        )
    )
    allAchievements.add(
        Achievement(
            id = "streak_7",
            title = "Konsisten Seminggu",
            description = "Capai 7 hari streak.",
            icon = Icons.Default.CheckCircle,
            isUnlocked = streak >= 7,
            progress = minOf(streak, 7),
            goal = 7
        )
    )
    allAchievements.add(
        Achievement(
            id = "streak_30",
            title = "Bulan Sempurna",
            description = "Capai 30 hari streak.",
            icon = Icons.Default.CheckCircle,
            isUnlocked = streak >= 30,
            progress = minOf(streak, 30),
            goal = 30
        )
    )

    // 3. Total XP Achievements
    allAchievements.add(
        Achievement(
            id = "xp_1000",
            title = "Kolektor XP",
            description = "Kumpulkan 1000 total XP.",
            icon = Icons.Default.EmojiEvents,
            isUnlocked = totalXp >= 1000,
            progress = minOf(totalXp, 1000),
            goal = 1000
        )
    )
    allAchievements.add(
        Achievement(
            id = "xp_5000",
            title = "Elite",
            description = "Kumpulkan 5000 total XP.",
            icon = Icons.Default.EmojiEvents,
            isUnlocked = totalXp >= 5000,
            progress = minOf(totalXp, 5000),
            goal = 5000
        )
    )

    // 4. Total Habits Completed
    allAchievements.add(
        Achievement(
            id = "habits_1",
            title = "Langkah Pertama",
            description = "Selesaikan misi pertamamu.",
            icon = Icons.Default.Check,
            isUnlocked = totalHabitsCompleted >= 1,
            progress = minOf(totalHabitsCompleted, 1),
            goal = 1
        )
    )
    allAchievements.add(
        Achievement(
            id = "habits_50",
            title = "Pekerja Keras",
            description = "Selesaikan 50 total misi.",
            icon = Icons.Default.List,
            isUnlocked = totalHabitsCompleted >= 50,
            progress = minOf(totalHabitsCompleted, 50),
            goal = 50
        )
    )
    allAchievements.add(
        Achievement(
            id = "habits_200",
            title = "Veteran Misi",
            description = "Selesaikan 200 total misi.",
            icon = Icons.Default.List,
            isUnlocked = totalHabitsCompleted >= 200,
            progress = minOf(totalHabitsCompleted, 200),
            goal = 200
        )
    )

    // Mengurutkan: yang belum selesai tapi ada progress di atas,
    // lalu yang belum selesai, lalu yang sudah selesai
    return allAchievements.sortedWith(
        compareBy(
            { it.isUnlocked }, // Selesai (true) di bawah
            { !(it.progress > 0 && !it.isUnlocked) } // Progress (true) di atas
        )
    )
}
// ------------------------------


class MainActivity : ComponentActivity() {
    private val viewModel: HabitViewModel by viewModels()

    // --- LOGIKA MUSIK LATAR ---
    private var mediaPlayer: MediaPlayer? = null

    // Fungsi untuk memulai musik
    private fun startBackgroundMusic() {
        if (mediaPlayer == null) {
            // Ganti R.raw.sountrack dengan nama file Anda
            mediaPlayer = MediaPlayer.create(this, R.raw.sountrack)
            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(0.5f, 0.5f)
        }
        try {
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        } catch (e: IllegalStateException) {
            Log.e("MainActivityMusic", "Error starting MediaPlayer: ${e.message}")
            releaseMediaPlayer()
            mediaPlayer = MediaPlayer.create(this, R.raw.sountrack) // Sesuaikan nama file
            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(0.5f, 0.5f)
            mediaPlayer?.start()
        }
    }

    // Fungsi untuk menghentikan sementara musik (saat app ke background)
    private fun pauseBackgroundMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    // Fungsi untuk melepaskan resource MediaPlayer (saat app ditutup)
    private fun releaseMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // --- Lifecycle Callbacks untuk Musik ---
    override fun onStart() {
        super.onStart()
        startBackgroundMusic()
    }

    override fun onStop() {
        super.onStop()
        pauseBackgroundMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }

    // Logic untuk meminta izin notifikasi
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

// Struktur data untuk template misi
data class HabitTemplate(val name: String, val schedule: String, val weight: Int)

// Daftar template misi (TERMASUK SIDE QUEST)
val predefinedHabitTemplates = listOf(
    // Kategori: Kesehatan Fisik
    "Kesehatan Fisik" to listOf(
        HabitTemplate("Minum 8 gelas air", "Setiap Hari", 10),
        HabitTemplate("Olahraga 30 menit", "Setiap Hari", 30),
        HabitTemplate("Tidur 7-8 jam", "Setiap Hari", 25),
        HabitTemplate("Makan sayur & buah", "Setiap Hari", 15),
        HabitTemplate("Tidak merokok", "Setiap Hari", 50),
        HabitTemplate("Jalan kaki 10.000 langkah", "Setiap Hari", 40),
        HabitTemplate("Tidak makan junk food", "Setiap Hari", 20),
        HabitTemplate("Sarapan sehat", "Setiap Hari", 15)
    ),
    // Kategori: Kesehatan Mental & Produktivitas
    "Produktivitas & Mental" to listOf(
        HabitTemplate("Meditasi 10 menit", "Setiap Hari", 20),
        HabitTemplate("Membaca buku 20 menit", "Setiap Hari", 20),
        HabitTemplate("Belajar hal baru 30 menit", "Setiap Hari", 30),
        HabitTemplate("Merencanakan hari (To-Do List)", "Setiap Pagi", 10),
        HabitTemplate("Tidak main medsos 1 jam sebelum tidur", "Setiap Malam", 25),
        HabitTemplate("Bangun pagi (sebelum jam 6)", "Setiap Pagi", 20),
        HabitTemplate("Menulis jurnal", "Setiap Hari", 15),
        HabitTemplate("Berlatih bersyukur", "Setiap Hari", 15)
    ),
    // Kategori: Keterampilan & Hobi
    "Keterampilan & Hobi" to listOf(
        HabitTemplate("Latihan coding 1 jam", "Setiap Hari", 35),
        HabitTemplate("Berlatih alat musik 30 menit", "Setiap Hari", 25),
        HabitTemplate("Menggambar/Melukis 30 menit", "Setiap Hari", 20),
        HabitTemplate("Belajar bahasa baru 20 menit", "Setiap Hari", 25),
        HabitTemplate("Menulis 500 kata", "Setiap Hari", 30)
    ),
    // Kategori: Tugas & Kebersihan
    "Tugas & Kebersihan" to listOf(
        HabitTemplate("Membersihkan/Merapikan kamar", "Setiap Hari", 20),
        HabitTemplate("Cuci piring", "Setiap Hari", 10),
        HabitTemplate("Menyelesaikan tugas utama", "Setiap Hari", 40),
        HabitTemplate("Membuang sampah", "Setiap Hari", 5)
    ),

    // --- KATEGORI SIDE QUEST (PINDAHAN DARI ACHIEVEMENTS) ---
    "Tantangan Sosial (Side Quest)" to listOf(
        HabitTemplate("Teman Baru", "Tugas Sekali", 50),
        HabitTemplate("Hubungi Teman Lama", "Tugas Sekali", 30)
    ),
    "Tantangan Penjelajah (Side Quest)" to listOf(
        HabitTemplate("Ambil Rute Baru", "Tugas Sekali", 20),
        HabitTemplate("Kunjungi Tempat Baru", "Tugas Sekali", 40)
    ),
    "Tantangan Pikiran (Side Quest)" to listOf(
        HabitTemplate("Baca 1 Bab Buku Non-Fiksi", "Tugas Sekali", 25),
        HabitTemplate("24 Jam Tanpa Keluhan", "Tugas Sekali", 100)
    ),
    "Tantangan Kebugaran (Side Quest)" to listOf(
        HabitTemplate("Peregangan Pagi 5 Menit", "Setiap Hari", 15),
        HabitTemplate("Pilih Naik Tangga", "Setiap Hari", 20)
    )
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: HabitViewModel,
    onScheduleReminderClick: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.resetHabitsIfNewDay()
    }

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // State untuk mengontrol 3 bottom sheet
    var showAddOptionsSheet by remember { mutableStateOf(false) } // Pilihan (manual/template)
    var showManualAddSheet by remember { mutableStateOf(false) }  // Form manual
    var showTemplateSheet by remember { mutableStateOf(false) }    // Daftar template

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
                        // Tampilkan sheet pilihan
                        showAddOptionsSheet = true
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
                // Edit langsung buka sheet manual
                showManualAddSheet = true
            },
            onDeleteClick = { habit ->
                habitToDelete = habit
            },
            onScheduleReminderClick = onScheduleReminderClick
        )
    }

    // Bottom Sheet 1 (Pilihan)
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
                        habitToEdit = null // Pastikan mode tambah baru
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

    // Bottom Sheet 2 (Form Manual)
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

    // Bottom Sheet 3 (Daftar Template)
    if (showTemplateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTemplateSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = DarkBackground // Pakai background utama
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
            // --- LOGIKA ACHIEVEMENT DIMASUKKAN DI SINI ---
            // Asumsi: ViewModel Anda harusnya melacak totalHabitsCompleted
            // Di sini kita pakai totalXp sebagai perkiraan kasar
            val mockTotalHabitsCompleted = (appData?.totalXp ?: 0) / 25 // Asumsi rata-rata 25 XP per misi

            val variedAchievements = getAllAchievements(
                level = appData?.level ?: 1,
                streak = appData?.streak ?: 0,
                totalXp = appData?.totalXp ?: 0,
                totalHabitsCompleted = mockTotalHabitsCompleted
            )
            // Menggunakan daftar yang bervariasi, bukan dari appData
            AchievementsScreen(achievements = variedAchievements)
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
fun StatCard(title: String, value: String, icon: ImageVector, iconColor: Color) {
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


// --- FUNGSI AchievementsScreen (TETAP SAMA, TAPI MENERIMA MODEL BARU) ---
@Composable
fun AchievementsScreen(achievements: List<Achievement>) { // Menggunakan model baru
    // State untuk mengontrol dialog
    var achievementToShowDesc by remember { mutableStateOf<Achievement?>(null) }

    // --- Kalkulasi Progress (STATISTIK KESELURUHAN) ---
    val unlockedCount = achievements.count { it.isUnlocked }
    val totalCount = achievements.size
    val progress = if (totalCount > 0) unlockedCount.toFloat() / totalCount.toFloat() else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Item 1: Judul Utama
        item {
            Text(
                "PENCAPAIAN",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextColorPrimary,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }

        // Item 2: Header Progress Card (Keseluruhan)
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

        // Item 3: Daftar Achievement
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
                AchievementItem( // Memanggil AchievementItem yang baru
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
        AchievementDescriptionDialog( // Memanggil Dialog yang baru
            achievement = achievementToShowDesc!!,
            onDismiss = { achievementToShowDesc = null }
        )
    }
}

// --- FUNGSI AchievementItem (DIMODIFIKASI UNTUK VARIASI) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementItem(
    achievement: Achievement, // Menggunakan model data baru
    onClick: () -> Unit
) {
    val borderColor = if (achievement.isUnlocked) AccentYellow else CardBackground
    val iconColor = if (achievement.isUnlocked) AccentYellow else TextColorSecondary
    val progress = if (achievement.goal > 0) achievement.progress.toFloat() / achievement.goal.toFloat() else 0f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(2.dp, borderColor),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Gunakan Column agar bisa menaruh progress bar
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF48484A)),
                    contentAlignment = Alignment.Center
                ) {
                    // BARU: Gunakan ikon dari data
                    Icon(achievement.icon, contentDescription = "Icon", tint = iconColor, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(achievement.title, color = TextColorPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

                    // BARU: Tampilkan progress teks jika belum unlock
                    if (!achievement.isUnlocked && achievement.goal > 1) {
                        Text(
                            "${achievement.progress} / ${achievement.goal}",
                            color = TextColorSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                if (achievement.isUnlocked) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Tercapai",
                        tint = AccentYellow,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // BARU: Tampilkan Progress Bar jika belum selesai dan punya goal > 1
            if (!achievement.isUnlocked && achievement.goal > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = AccentYellow,
                    trackColor = Color(0xFF48484A)
                )
            }
        }
    }
}

// --- COMPOSABLE DIALOG (DIMODIFIKASI) ---
@Composable
fun AchievementDescriptionDialog(
    achievement: Achievement, // Menggunakan model data baru
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text(
                text = achievement.title,
                color = if (achievement.isUnlocked) AccentYellow else TextColorPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            // BARU: Tampilkan deskripsi dan progress
            Column {
                Text(
                    text = achievement.description,
                    color = TextColorSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Progress: ${achievement.progress} / ${achievement.goal}",
                    color = if (achievement.isUnlocked) AccentYellow else TextColorPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Tutup", color = PrimaryColor)
            }
        }
    )
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

@Composable
fun AddOptionsSheet(
    onManualAddClick: () -> Unit,
    onTemplateAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tambah Misi",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextColorPrimary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = onTemplateAddClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text("Pilih dari Template", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onManualAddClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, PrimaryColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text("Buat Misi Sendiri (Manual)", color = PrimaryColor, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TemplateHabitSheet(
    templates: List<Pair<String, List<HabitTemplate>>>,
    onTemplateClick: (HabitTemplate) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize() // Mengisi ruang bottom sheet
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "Pilih Misi dari Template",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextColorPrimary,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        templates.forEach { (category, habitList) ->
            item {
                Text(
                    text = category.uppercase(),
                    color = TextColorSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(habitList, key = { it.name }) { template ->
                HabitTemplateItem(
                    template = template,
                    onClick = { onTemplateClick(template) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp)) // Spacer di akhir
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTemplateItem(
    template: HabitTemplate,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(template.name, color = TextColorPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(template.schedule, color = TextColorSecondary, fontSize = 12.sp)
            }
            Spacer(Modifier.width(16.dp))
            Text(
                "+${template.weight} XP",
                color = AccentYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAddHabitSheet(
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
            text = if (habitToEdit == null) "Tambah Misi Manual" else "Edit Misi",
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