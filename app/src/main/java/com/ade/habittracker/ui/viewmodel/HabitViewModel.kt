package com.ade.habittracker.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ade.habittracker.R
import com.ade.habittracker.data.HabitRepository
import com.ade.habittracker.model.Achievement
import com.ade.habittracker.model.AppData
import com.ade.habittracker.model.HabitHistoryItem
import com.ade.habittracker.notification.HabitReminderWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// Data class untuk Avatar
data class AvatarItem(
    val id: String,
    @DrawableRes val resId: Int,
    val requiredLevel: Int
)

// Data class untuk Pilihan Gelar
data class TitleItem(
    val title: String,
    val requiredLevel: Int
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HabitRepository(application.applicationContext)

    // --- DAFTAR SEMUA AVATAR ---
    private val allAvatars = listOf(
        AvatarItem("avatar_level1", R.drawable.avatar_level1, 1),
        AvatarItem("avatar_level5", R.drawable.avatar_level5, 5),
        AvatarItem("avatar_level10", R.drawable.avatar_level10, 10),
        AvatarItem("avatar_level15", R.drawable.avatar_level20, 15),
        AvatarItem("avatar_level20", R.drawable.avatar_master, 20)
    )

    // --- DAFTAR SEMUA GELAR ---
    private val allTitles = listOf(
        TitleItem("Petualang Baru", 1),
        TitleItem("Pemula Produktif", 5),
        TitleItem("Pejuang Disiplin", 10),
        TitleItem("Ahli Kebiasaan", 15),
        TitleItem("Sang Legenda", 20)
    )

    // --- STATE FLOW UTAMA ---
    val appData: StateFlow<AppData?> = repository.appData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // --- STATE FLOW KHUSUS RIWAYAT (HISTORY) ---
    val habitHistory: StateFlow<List<HabitHistoryItem>> = appData.map { data ->
        data?.history?.sortedByDescending { it.timestamp } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- LIST ACHIEVEMENT ---
    val achievements: StateFlow<List<Achievement>> = appData.map { data ->
        if (data == null) emptyList()
        else getAllAchievements(data.level, data.streak, data.totalXp, data.totalHabitsCompleted)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- USER PROFILE ---
    val userName: StateFlow<String> = appData.map { data ->
        data?.userName ?: "Petualang"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Petualang")

    val userTitle: StateFlow<String> = appData.map { data ->
        data?.userTitle ?: "Petualang Baru"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Petualang Baru")

    val avatarListWithLockStatus: StateFlow<List<Pair<AvatarItem, Boolean>>> = appData.map { data ->
        val currentLevel = data?.level ?: 1
        allAvatars.map { avatar ->
            avatar to (currentLevel >= avatar.requiredLevel)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val titleListWithLockStatus: StateFlow<List<Pair<TitleItem, Boolean>>> = appData.map { data ->
        val currentLevel = data?.level ?: 1
        allTitles.map { title ->
            title to (currentLevel >= title.requiredLevel)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profileImageResId: StateFlow<Int> = appData.map { data ->
        val savedId = data?.profileImageId ?: "avatar_level1"
        allAvatars.find { it.id == savedId }?.resId ?: R.drawable.avatar_level1
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), R.drawable.avatar_level1)


    // --- FUNGSI UPDATE SETTINGS (UPDATE) ---

    // 1. Musik ON/OFF
    fun setMusicEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            repository.saveAppData(currentData.copy(isMusicEnabled = isEnabled))
        }
    }

    // 2. Chibi ON/OFF
    fun setChibiEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            repository.saveAppData(currentData.copy(isChibiEnabled = isEnabled))
        }
    }

    // --- FUNGSI UPDATE PROFIL ---

    fun updateUserName(newName: String) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            if(newName.isNotBlank()) {
                repository.saveAppData(currentData.copy(userName = newName))
            }
        }
    }

    fun updateProfileImageId(newId: String) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            repository.saveAppData(currentData.copy(profileImageId = newId))
        }
    }

    fun updateUserTitle(newTitle: String) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            repository.saveAppData(currentData.copy(userTitle = newTitle))
        }
    }

    // --- LOGIK HABIT, PROGRESS, RESET HARIAN & BULANAN ---

    fun resetHabitsIfNewDay() {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch

            val dailyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val monthlyFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault()) // Format Bulan

            val now = Date()
            val todayStr = dailyFormat.format(now)
            val currentMonthStr = monthlyFormat.format(now)

            var updatedData = currentData

            // 1. CEK RESET BULANAN (Season Baru)
            if (updatedData.lastMonthlyResetDate != currentMonthStr) {
                // Hanya reset jika ini bukan install pertama (field tidak kosong)
                if (updatedData.lastMonthlyResetDate.isNotEmpty()) {
                    updatedData = updatedData.copy(
                        level = 1,      // Reset Level ke 1
                        totalXp = 0     // Reset XP ke 0
                        // Streak dan Total Habits TIDAK direset agar user tetap semangat
                    )
                }
                // Update penanda bulan terakhir
                updatedData = updatedData.copy(lastMonthlyResetDate = currentMonthStr)
            }

            // 2. CEK RESET HARIAN
            if (updatedData.lastResetDate != todayStr) {
                val resetHabits = updatedData.habits.map { it.copy(isCompleted = false) }

                // Tambah hari login
                val newTotalLogin = updatedData.totalLoginDays + 1

                updatedData = updatedData.copy(
                    habits = resetHabits,
                    lastResetDate = todayStr,
                    totalLoginDays = newTotalLogin
                )
            }

            // Simpan perubahan jika ada data yang berubah
            if (updatedData != currentData) {
                repository.saveAppData(updatedData)
            }
        }
    }

    fun addHabit(name: String, schedule: String, weight: Int) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val newHabit = currentData.habits.toMutableList().apply {
                add(
                    com.ade.habittracker.model.Habit(
                        id = (currentData.habits.maxOfOrNull { it.id } ?: 0) + 1,
                        name = name,
                        schedule = schedule,
                        weight = weight
                    )
                )
            }
            repository.saveAppData(currentData.copy(habits = newHabit))
        }
    }

    fun updateHabit(id: Int, name: String, schedule: String, weight: Int) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val updatedHabits = currentData.habits.map {
                if (it.id == id) {
                    it.copy(name = name, schedule = schedule, weight = weight)
                } else {
                    it
                }
            }
            repository.saveAppData(currentData.copy(habits = updatedHabits))
        }
    }

    fun deleteHabit(id: Int) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val habitToDelete = currentData.habits.find { it.id == id }
            var newTotalXp = currentData.totalXp
            var newTotalHabitsCompleted = currentData.totalHabitsCompleted

            if (habitToDelete?.isCompleted == true) {
                newTotalXp -= habitToDelete.weight
                newTotalHabitsCompleted -= 1
            }

            if (newTotalXp < 0) newTotalXp = 0
            if (newTotalHabitsCompleted < 0) newTotalHabitsCompleted = 0

            val updatedHabits = currentData.habits.filterNot { it.id == id }
            val newLevel = calculateLevel(newTotalXp)
            repository.saveAppData(
                currentData.copy(
                    habits = updatedHabits,
                    totalXp = newTotalXp,
                    level = newLevel,
                    totalHabitsCompleted = newTotalHabitsCompleted
                )
            )
        }
    }

    // 🔥 LOGIC UTAMA: ONE-WAY CHECK (TIDAK BISA UNCHECK) 🔥
    fun toggleHabitCompleted(habitId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch

            val targetHabit = currentData.habits.find { it.id == habitId } ?: return@launch

            // CEGAH UNCHECK:
            if (targetHabit.isCompleted && !isCompleted) {
                return@launch
            }

            var newTotalXp = currentData.totalXp
            var newTotalHabitsCompleted = currentData.totalHabitsCompleted
            val currentHistory = currentData.history.toMutableList()

            val updatedHabits = currentData.habits.map { habit ->
                if (habit.id == habitId) {
                    // Jika user mencentang (dari False ke True)
                    if (isCompleted && !habit.isCompleted) {
                        newTotalXp += habit.weight
                        newTotalHabitsCompleted += 1

                        // Catat ke History
                        currentHistory.add(
                            HabitHistoryItem(
                                id = UUID.randomUUID().toString(),
                                habitName = habit.name,
                                xpEarned = habit.weight,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        habit.copy(isCompleted = true)
                    } else {
                        habit
                    }
                } else {
                    habit
                }
            }

            if (newTotalXp < 0) newTotalXp = 0
            if (newTotalHabitsCompleted < 0) newTotalHabitsCompleted = 0

            val newLevel = calculateLevel(newTotalXp)
            val allHabitsCompleted = updatedHabits.isNotEmpty() && updatedHabits.all { it.isCompleted }

            val dataWithNewProgress = currentData.copy(
                habits = updatedHabits,
                totalXp = newTotalXp,
                level = newLevel,
                totalHabitsCompleted = newTotalHabitsCompleted,
                history = currentHistory
            )

            val finalData = if (allHabitsCompleted) {
                checkStreaks(dataWithNewProgress)
            } else {
                dataWithNewProgress
            }

            repository.saveAppData(finalData)
        }
    }

    private fun checkStreaks(currentData: AppData): AppData {
        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(today.time)

        if (todayStr == currentData.lastCompletionDate) {
            return currentData
        }

        val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        val yesterdayStr = dateFormat.format(yesterday.time)

        val newStreak = if (currentData.lastCompletionDate == yesterdayStr) {
            currentData.streak + 1
        } else {
            1
        }
        val newLastCompletionDate = todayStr

        return currentData.copy(
            streak = newStreak,
            lastCompletionDate = newLastCompletionDate
        )
    }

    private fun calculateLevel(totalXp: Int): Int {
        return (totalXp / 100) + 1
    }

    fun getXpProgress(): Pair<Int, Int> {
        val totalXp = appData.value?.totalXp ?: 0
        val currentLevelXp = (appData.value?.level?.minus(1) ?: 0) * 100
        val progress = totalXp - currentLevelXp
        return Pair(progress, 100)
    }

    fun scheduleDailyReminder(context: Context) {
        val reminderRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(3, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_habit_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }

    // --- FUNGSI GENERATE ACHIEVEMENT ---
    private fun getAllAchievements(
        level: Int,
        streak: Int,
        totalXp: Int,
        totalHabitsCompleted: Int
    ): List<Achievement> {
        val allAchievements = mutableListOf<Achievement>()

        // KATEGORI 1: Level Achievements
        allAchievements.add(Achievement("level_5", "Kekuatan Baru", "Tunjukkan potensimu dan capai Level 5.", R.drawable.level5, level >= 5, minOf(level, 5), 5))
        allAchievements.add(Achievement("level_10", "Pejuang Tangguh", "Disiplin adalah senjatamu. Capai Level 10.", R.drawable.level10, level >= 10, minOf(level, 10), 10))
        allAchievements.add(Achievement("level_20", "Legenda Hidup", "Kamu telah menguasai dirimu. Capai Level 20.", R.drawable.level20, level >= 20, minOf(level, 20), 20))

        // KATEGORI 2: Streak Achievements
        allAchievements.add(Achievement("streak_3", "Api Mulai Menyala", "Jaga apinya tetap menyala selama 3 hari beruntun.", R.drawable.streak3, streak >= 3, minOf(streak, 3), 3))
        allAchievements.add(Achievement("streak_7", "Kekuatan Kebiasaan", "Kamu tak terhentikan! Selesaikan 7 hari streak.", R.drawable.streak7, streak >= 7, minOf(streak, 7), 7))
        allAchievements.add(Achievement("streak_30", "Penguasa Waktu", "Satu bulan penuh dedikasi. Capai 30 hari streak.", R.drawable.streak30, streak >= 30, minOf(streak, 30), 30))

        // KATEGORI 3: Total XP Achievements
        allAchievements.add(Achievement("xp_1000", "Pemburu Poin", "Setiap poin berharga. Kumpulkan 1000 total XP.", R.drawable.xp1000, totalXp >= 1000, minOf(totalXp, 1000), 1000))
        allAchievements.add(Achievement("xp_5000", "Veteran Elit", "Hanya untuk yang terkuat. Kumpulkan 5000 total XP.", R.drawable.xp5000, totalXp >= 5000, minOf(totalXp, 5000), 5000))

        // KATEGORI 4: Total Habits Completed
        allAchievements.add(Achievement("habits_1", "Awal Perjalanan", "Perjalanan seribu mil dimulai dengan satu misi.", R.drawable.misi1, totalHabitsCompleted >= 1, minOf(totalHabitsCompleted, 1), 1))
        allAchievements.add(Achievement("habits_50", "Ksatria Produktif", "Terus maju! Selesaikan 50 total misi.", R.drawable.misi50, totalHabitsCompleted >= 50, minOf(totalHabitsCompleted, 50), 50))
        allAchievements.add(Achievement("habits_200", "Sang Penakluk Misi", "Tidak ada misi yang terlalu sulit. Selesaikan 200 misi.", R.drawable.misi200, totalHabitsCompleted >= 200, minOf(totalHabitsCompleted, 200), 200))

        return allAchievements.sortedWith(compareBy({ it.isUnlocked }, { !(it.progress > 0 && !it.isUnlocked) }))
    }
}