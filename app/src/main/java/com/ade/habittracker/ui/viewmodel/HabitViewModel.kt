package com.ade.habittracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ade.habittracker.data.HabitRepository
import com.ade.habittracker.model.AppData
import com.ade.habittracker.model.Habit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    // 1. ViewModel sekarang hanya tahu tentang Repository
    private val repository = HabitRepository(application)

    // 2. INI ADALAH PERUBAHAN UTAMA:
    // Kita langsung mengubah Flow dari Repository menjadi StateFlow.
    // Tidak perlu lagi init block, _appData, atau collect manual.
    // repository.appDataFlow sudah memberikan kita data AppData? yang benar.
    val appData: StateFlow<AppData?> = repository.appDataFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun addHabit(name: String, schedule: String, weight: Int) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch
            val newHabit = Habit(
                id = (currentData.habits.maxOfOrNull { it.id } ?: 0) + 1,
                name = name,
                schedule = schedule,
                weight = weight
            )
            val updatedHabits = currentData.habits + newHabit
            repository.saveAppData(currentData.copy(habits = updatedHabits))
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

            val newLevel = calculateLevel(newTotalXp)
            val dataAfterXp = currentData.copy(habits = updatedHabits, totalXp = newTotalXp, level = newLevel)

            val finalData = checkStreaksAndAchievements(dataAfterXp)

            repository.saveAppData(finalData)
        }
    }

    private fun checkStreaksAndAchievements(currentData: AppData): AppData {
        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()
        var newStreak = currentData.streak
        var newLastCompletionDate = currentData.lastCompletionDate

        val anyHabitCompleted = currentData.habits.any { it.isCompleted }

        if (anyHabitCompleted) {
            if (newLastCompletionDate == null) {
                newStreak = 1
                newLastCompletionDate = today
            } else if (newLastCompletionDate == yesterday) {
                newStreak += 1
                newLastCompletionDate = today
            } else if (newLastCompletionDate != today) {
                newStreak = 1
                newLastCompletionDate = today
            }
        }

        val newAchievements = currentData.achievements.map { achievement ->
            if (achievement.isUnlocked) return@map achievement

            var unlocked = false
            when (achievement.id) {
                1 -> if (currentData.totalXp > 0) unlocked = true
                2 -> if (newStreak >= 7) unlocked = true
                3 -> if (currentData.level >= 10) unlocked = true
                4 -> {
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
        val currentData = appData.value ?: return Pair(0, 100)
        val currentLevel = currentData.level
        val xpForCurrentLevel = (currentLevel - 1) * 100
        val currentXpInLevel = currentData.totalXp - xpForCurrentLevel
        return Pair(currentXpInLevel, 100)
    }

    private fun getTodayDateString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun getYesterdayDateString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(calendar.time)
    }
}

