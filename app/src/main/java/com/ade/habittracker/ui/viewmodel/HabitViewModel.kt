package com.ade.habittracker.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ade.habittracker.R
import com.ade.habittracker.data.HabitRepository
import com.ade.habittracker.model.AppData
import com.ade.habittracker.model.Achievement
import com.ade.habittracker.notification.HabitReminderWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class AvatarItem(
    val id: String,
    @DrawableRes val resId: Int,
    val requiredLevel: Int
)

// --- TAMBAHAN BARU: Data class untuk Pilihan Gelar ---
data class TitleItem(
    val title: String,
    val requiredLevel: Int
)
// ----------------------------------------------------

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HabitRepository(application.applicationContext)

    // --- DAFTAR SEMUA AVATAR ---
    private val allAvatars = listOf(
        // TODO: Ganti R.drawable.ic_launcher_foreground dengan nama file PNG Anda
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
    // ---------------------------

    val appData: StateFlow<AppData?> = repository.appData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val achievements: StateFlow<List<Achievement>> = appData.map { data ->
        if (data == null) emptyList()
        else getAllAchievements(data.level, data.streak, data.totalXp, data.totalHabitsCompleted)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- STATEFLOW PROFIL (DIPERBARUI) ---

    val userName: StateFlow<String> = appData.map { data ->
        data?.userName ?: "Petualang"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Petualang")

    // (DIPERBARUI) StateFlow ini sekarang membaca GELAR YANG DISIMPAN
    val userTitle: StateFlow<String> = appData.map { data ->
        data?.userTitle ?: "Petualang Baru"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Petualang Baru")

    // StateFlow untuk daftar avatar yang SUDAH TERBUKA
    val avatarListWithLockStatus: StateFlow<List<Pair<AvatarItem, Boolean>>> = appData.map { data ->
        val currentLevel = data?.level ?: 1
        allAvatars.map { avatar ->
            avatar to (currentLevel >= avatar.requiredLevel)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // (BARU) StateFlow untuk daftar gelar yang SUDAH TERBUKA
    val titleListWithLockStatus: StateFlow<List<Pair<TitleItem, Boolean>>> = appData.map { data ->
        val currentLevel = data?.level ?: 1
        allTitles.map { title ->
            title to (currentLevel >= title.requiredLevel)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // StateFlow ini tetap sama
    val profileImageResId: StateFlow<Int> = appData.map { data ->
        val savedId = data?.profileImageId ?: "avatar_level1"
        allAvatars.find { it.id == savedId }?.resId ?: R.drawable.ic_launcher_foreground
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), R.drawable.ic_launcher_foreground)

    // ---------------------------------------------

    // --- FUNGSI UPDATE PROFIL (DIPERBARUI) ---
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

    // (BARU) Fungsi untuk update gelar
    fun updateUserTitle(newTitle: String) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            repository.saveAppData(currentData.copy(userTitle = newTitle))
        }
    }
    // -----------------------------------------

    // ... (Sisa kode ViewModel: resetHabitsIfNewDay, addHabit, dll... tetap sama) ...
    // (Salin dari kode Anda sebelumnya)
    fun resetHabitsIfNewDay() {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date())

            if (currentData.lastResetDate != todayStr) {
                val resetHabits = currentData.habits.map { it.copy(isCompleted = false) }
                val newData = currentData.copy(
                    habits = resetHabits,
                    lastResetDate = todayStr
                )
                repository.saveAppData(newData)
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

    fun toggleHabitCompleted(habitId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch

            var newTotalXp = currentData.totalXp
            var newTotalHabitsCompleted = currentData.totalHabitsCompleted

            val updatedHabits = currentData.habits.map {
                if (it.id == habitId) {
                    if (isCompleted && !it.isCompleted) {
                        newTotalXp += it.weight
                        newTotalHabitsCompleted += 1
                    } else if (!isCompleted && it.isCompleted) {
                        newTotalXp -= it.weight
                        newTotalHabitsCompleted -= 1
                    }
                    it.copy(isCompleted = isCompleted)
                } else {
                    it
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
                totalHabitsCompleted = newTotalHabitsCompleted
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
        val reminderRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_habit_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }

    private fun getAllAchievements(
        level: Int,
        streak: Int,
        totalXp: Int,
        totalHabitsCompleted: Int
    ): List<Achievement> {
        val allAchievements = mutableListOf<Achievement>()

        // 1. Level Achievements
        allAchievements.add(
            Achievement(
                id = "level_5", title = "Kekuatan Baru", description = "Tunjukkan potensimu dan capai Level 5.",
                icon = Icons.Default.Star, isUnlocked = level >= 5,
                progress = minOf(level, 5), goal = 5
            )
        )
        allAchievements.add(
            Achievement(
                id = "level_10", title = "Pejuang Tangguh", description = "Disiplin adalah senjatamu. Capai Level 10.",
                icon = Icons.Default.Star, isUnlocked = level >= 10,
                progress = minOf(level, 10), goal = 10
            )
        )
        allAchievements.add(
            Achievement(
                id = "level_20", title = "Legenda Hidup", description = "Kamu telah menguasai dirimu. Capai Level 20.",
                icon = Icons.Default.Star, isUnlocked = level >= 20,
                progress = minOf(level, 20), goal = 20
            )
        )

        // 2. Streak Achievements
        allAchievements.add(
            Achievement(
                id = "streak_3", title = "Api Mulai Menyala", description = "Jaga apinya tetap menyala selama 3 hari beruntun.",
                icon = Icons.Default.CheckCircle, isUnlocked = streak >= 3,
                progress = minOf(streak, 3), goal = 3
            )
        )
        allAchievements.add(
            Achievement(
                id = "streak_7", title = "Kekuatan Kebiasaan", description = "Kamu tak terhentikan! Selesaikan 7 hari streak.",
                icon = Icons.Default.CheckCircle, isUnlocked = streak >= 7,
                progress = minOf(streak, 7), goal = 7
            )
        )
        allAchievements.add(
            Achievement(
                id = "streak_30", title = "Penguasa Waktu", description = "Satu bulan penuh dedikasi. Capai 30 hari streak.",
                icon = Icons.Default.CheckCircle, isUnlocked = streak >= 30,
                progress = minOf(streak, 30), goal = 30
            )
        )

        // 3. Total XP Achievements
        allAchievements.add(
            Achievement(
                id = "xp_1000", title = "Pemburu Poin", description = "Setiap poin berharga. Kumpulkan 1000 total XP.",
                icon = Icons.Default.EmojiEvents, isUnlocked = totalXp >= 1000,
                progress = minOf(totalXp, 1000), goal = 1000
            )
        )
        allAchievements.add(
            Achievement(
                id = "xp_5000", title = "Veteran Elit", description = "Hanya untuk yang terkuat. Kumpulkan 5000 total XP.",
                icon = Icons.Default.EmojiEvents, isUnlocked = totalXp >= 5000,
                progress = minOf(totalXp, 5000), goal = 5000
            )
        )

        // 4. Total Habits Completed
        allAchievements.add(
            Achievement(
                id = "habits_1", title = "Awal Perjalanan", description = "Perjalanan seribu mil dimulai dengan satu misi.",
                icon = Icons.Default.Check, isUnlocked = totalHabitsCompleted >= 1,
                progress = minOf(totalHabitsCompleted, 1), goal = 1
            )
        )
        allAchievements.add(
            Achievement(
                id = "habits_50", title = "Ksatria Produktif", description = "Terus maju! Selesaikan 50 total misi.",
                icon = Icons.Default.List, isUnlocked = totalHabitsCompleted >= 50,
                progress = minOf(totalHabitsCompleted, 50), goal = 50
            )
        )
        allAchievements.add(
            Achievement(
                id = "habits_200", title = "Sang Penakluk Misi", description = "Tidak ada misi yang terlalu sulit. Selesaikan 200 misi.",
                icon = Icons.Default.List, isUnlocked = totalHabitsCompleted >= 200,
                progress = minOf(totalHabitsCompleted, 200), goal = 200
            )
        )

        // Mengurutkan
        return allAchievements.sortedWith(
            compareBy(
                { it.isUnlocked }, // Selesai (true) di bawah
                { !(it.progress > 0 && !it.isUnlocked) } // Progress (true) di atas
            )
        )
    }
}