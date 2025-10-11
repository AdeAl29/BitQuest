package com.ade.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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

// Warna tema sesuai screenshot (dark theme)
val DarkBackground = Color(0xFF2C2C2E)
val CardBackground = Color(0xFF3A3A3C)
val PrimaryColor = Color(0xFFFFD600) // Kuning untuk highlight
val TextColorPrimary = Color.White
val TextColorSecondary = Color(0xFF8E8E93)
val CheckboxColor = Color(0xFF48D1CC)

class MainActivity : ComponentActivity() {
    // Inisialisasi ViewModel
    private val viewModel: HabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitTrackerTheme {
                // Ambil data dari ViewModel
                val appData by viewModel.appData.collectAsStateWithLifecycle()

                if (appData != null) {
                    MainScreen(viewModel = viewModel, appData = appData!!)
                } else {
                    // Tampilkan loading indicator saat data sedang dimuat
                    Box(modifier = Modifier.fillMaxSize().background(DarkBackground), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                }
            }
        }
    }
}


// Composable utama yang mengatur seluruh layar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: HabitViewModel, appData: com.ade.habittracker.model.AppData) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) },
        floatingActionButton = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute == "habits") {
                FloatingActionButton(
                    onClick = { /* TODO: Logika untuk menambah habit baru */ },
                    containerColor = CheckboxColor,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, "Tambah Habit", tint = Color.White)
                }
            }
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavigationHost(navController = navController, viewModel = viewModel, appData = appData)
        }
    }
}

// Composable untuk Navigasi Halaman
@Composable
fun NavigationHost(
    navController: NavHostController,
    viewModel: HabitViewModel,
    appData: com.ade.habittracker.model.AppData
) {
    NavHost(navController = navController, startDestination = "habits") {
        composable("habits") { HabitsScreen(habits = appData.habits, onHabitToggled = viewModel::toggleHabitCompleted) }
        composable("stats") { StatsScreen(appData = appData, viewModel = viewModel) }
        composable("achievements") { AchievementsScreen(achievements = appData.achievements) }
    }
}

// Composable untuk Bottom Navigation Bar (Sama seperti sebelumnya, tidak ada perubahan)
@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("habits", "Habits", Icons.Default.Checklist),
        BottomNavItem("stats", "Statistik", Icons.Default.BarChart),
        BottomNavItem("achievements", "Pencapaian", Icons.Default.EmojiEvents)
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
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
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
data class BottomNavItem(val route: String, val title: String, val icon: ImageVector)


// --- Halaman Daftar Misi Harian (Habits) ---
@Composable
fun HabitsScreen(habits: List<Habit>, onHabitToggled: (Int, Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "DAFTAR MISI HARIAN",
            color = TextColorPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(habits, key = { it.id }) { habit ->
                HabitItem(
                    habit = habit,
                    onCheckedChange = { isChecked ->
                        onHabitToggled(habit.id, isChecked)
                    }
                )
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = if (habit.isCompleted) BorderStroke(2.dp, CheckboxColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = habit.isCompleted,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = CheckboxColor,
                    uncheckedColor = TextColorSecondary,
                    checkmarkColor = DarkBackground
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = habit.name, color = TextColorPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(text = habit.schedule, color = TextColorSecondary, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "+${habit.weight}",
                    color = PrimaryColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "XP",
                    color = PrimaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

// --- Halaman Profil & Statistik ---
@Composable
fun StatsScreen(appData: com.ade.habittracker.model.AppData, viewModel: HabitViewModel) {
    val (currentXp, totalXpForLevel) = viewModel.getXpProgress()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PROFIL & STATS",
            color = TextColorPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 24.dp)
        )

        LevelIndicator(level = appData.level, currentXp = currentXp, totalXpForLevel = totalXpForLevel)

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "STREAK",
                value = "${appData.streak} HARI",
                icon = { Icon(Icons.Default.Checklist, contentDescription = "Streak", tint = Color.Red, modifier = Modifier.size(32.dp)) },
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "TOTAL XP",
                value = appData.totalXp.toString(),
                icon = { Icon(Icons.Rounded.Star, contentDescription = "Total XP", tint = PrimaryColor, modifier = Modifier.size(32.dp)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun LevelIndicator(
    level: Int,
    currentXp: Int,
    totalXpForLevel: Int,
    strokeWidth: Dp = 12.dp,
    size: Dp = 200.dp
) {
    val progress = if (totalXpForLevel > 0) currentXp.toFloat() / totalXpForLevel.toFloat() else 0f
    val sweepAngle = 360 * progress

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background circle
            drawArc(
                color = CardBackground,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            // Progress arc
            drawArc(
                color = PrimaryColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("LEVEL", fontSize = 18.sp, color = TextColorSecondary)
            Text(
                text = "$level",
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                color = TextColorPrimary
            )
            Text("$currentXp / $totalXpForLevel XP", fontSize = 16.sp, color = TextColorSecondary)
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = TextColorSecondary, fontSize = 14.sp)
            Text(value, color = TextColorPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// --- Halaman Pencapaian ---
@Composable
fun AchievementsScreen(achievements: List<Achievement>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "PENCAPAIAN",
            color = TextColorPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(achievements) { achievement ->
                AchievementItem(achievement = achievement)
            }
        }
    }
}

@Composable
fun AchievementItem(achievement: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground,
            contentColor = if (achievement.isUnlocked) TextColorPrimary else TextColorSecondary
        ),
        border = if (achievement.isUnlocked) BorderStroke(2.dp, PrimaryColor) else null
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
                    .background(if (achievement.isUnlocked) PrimaryColor.copy(alpha = 0.2f) else CardBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Star,
                    contentDescription = "Achievement Icon",
                    tint = if (achievement.isUnlocked) PrimaryColor else TextColorSecondary,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = achievement.description,
                    fontSize = 14.sp,
                )
            }
            if (achievement.isUnlocked) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = "Unlocked",
                    tint = PrimaryColor,
                    modifier = Modifier.padding(start=8.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    // Preview tidak akan berfungsi sempurna karena butuh ViewModel
}

