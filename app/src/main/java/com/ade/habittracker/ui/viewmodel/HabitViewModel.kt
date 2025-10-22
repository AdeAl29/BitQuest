package com.ade.habittracker.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ade.habittracker.data.HabitRepository
import com.ade.habittracker.model.AppData
import com.ade.habittracker.notification.HabitReminderWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HabitRepository(application.applicationContext)

    val appData: StateFlow<AppData?> = repository.appData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // --- FUNGSI BARU UNTUK LOGIKA RESET HARIAN ---
    // Fungsi ini akan dipanggil dari MainActivity saat aplikasi pertama kali dibuka.
    fun resetHabitsIfNewDay() {
        viewModelScope.launch {
            // Ambil data saat ini, jika tidak ada, hentikan.
            val currentData = appData.value ?: return@launch
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date())

            // Cek: Apakah tanggal reset terakhir BUKAN hari ini?
            if (currentData.lastResetDate != todayStr) {
                // Jika ya, buat daftar habit baru dengan semua 'isCompleted' di-set ke false.
                val resetHabits = currentData.habits.map { it.copy(isCompleted = false) }

                // Buat data baru dengan habit yang sudah direset dan tanggal reset yang baru.
                // PENTING: Poin, level, streak, dan achievement TIDAK diubah!
                val newData = currentData.copy(
                    habits = resetHabits,
                    lastResetDate = todayStr
                )
                // Simpan data yang sudah diperbarui ke DataStore.
                repository.saveAppData(newData)
            }
            // Jika tanggalnya sama, tidak ada yang perlu dilakukan.
        }
    }
    // ---------------------------------------------

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
            if (habitToDelete?.isCompleted == true) {
                newTotalXp -= habitToDelete.weight
                if (newTotalXp < 0) newTotalXp = 0
            }

            val updatedHabits = currentData.habits.filterNot { it.id == id }
            val newLevel = calculateLevel(newTotalXp)
            repository.saveAppData(currentData.copy(habits = updatedHabits, totalXp = newTotalXp, level = newLevel))
        }
    }

    fun toggleHabitCompleted(habitId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch

            var newTotalXp = currentData.totalXp
            val updatedHabits = currentData.habits.map {
                if (it.id == habitId) {
                    val xpChange = if (isCompleted) it.weight else -it.weight
                    newTotalXp += xpChange
                    it.copy(isCompleted = isCompleted)
                } else {
                    it
                }
            }

            if (newTotalXp < 0) newTotalXp = 0
            val newLevel = calculateLevel(newTotalXp)

            val dataWithNewProgress = currentData.copy(
                habits = updatedHabits,
                totalXp = newTotalXp,
                level = newLevel
            )

            val finalData = if (isCompleted) {
                checkStreaksAndAchievements(dataWithNewProgress)
            } else {
                dataWithNewProgress
            }

            repository.saveAppData(finalData)
        }
    }

    private fun checkStreaksAndAchievements(currentData: AppData): AppData {
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

        val newAchievements = currentData.achievements.map { achievement ->
            if (achievement.isUnlocked) return@map achievement

            var unlocked = false
            when (achievement.id) {
                1 -> if (currentData.totalXp > 0) unlocked = true // Pemula
                2 -> if (newStreak >= 7) unlocked = true // Konsisten
                3 -> if (currentData.level >= 10) unlocked = true // Master Habit
                4 -> { // Rajin Belajar
                    val kotlinHabit = currentData.habits.find { it.name.contains("Belajar", ignoreCase = true) }
                    if (kotlinHabit?.isCompleted == true) {
                        unlocked = true
                    }
                }
            }
            if (unlocked) achievement.copy(isUnlocked = true) else achievement
        }

        return currentData.copy(
            streak = newStreak,
            lastCompletionDate = newLastCompletionDate,
            achievements = newAchievements
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
            // Anda bisa hapus komentar di bawah jika ingin notifikasi pertama muncul setelah delay tertentu
            // .setInitialDelay(8, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_habit_reminder",
            ExistingPeriodicWorkPolicy.KEEP, // Mencegah duplikasi jika fungsi ini dipanggil lagi
            reminderRequest
        )
    }
}

