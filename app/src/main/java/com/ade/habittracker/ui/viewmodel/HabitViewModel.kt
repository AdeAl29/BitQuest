package com.ade.habittracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ade.habittracker.data.DataStoreManager
import com.ade.habittracker.model.AppData
import com.ade.habittracker.model.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application)

    private val _appData = MutableStateFlow<AppData?>(null)
    val appData: StateFlow<AppData?> = _appData.asStateFlow()

    init {
        viewModelScope.launch {
            dataStoreManager.appDataFlow.collect { data ->
                _appData.value = data
            }
        }
    }

    fun toggleHabitCompleted(habitId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch

            var newTotalXp = currentData.totalXp
            val habit = currentData.habits.find { it.id == habitId } ?: return@launch

            if (isCompleted) {
                newTotalXp += habit.weight
            } else {
                newTotalXp -= habit.weight
            }
            if (newTotalXp < 0) newTotalXp = 0

            val updatedHabits = currentData.habits.map {
                if (it.id == habitId) {
                    it.copy(isCompleted = isCompleted)
                } else {
                    it
                }
            }

            var newLevel = currentData.level
            val xpForNextLevel = newLevel * 100
            if (newTotalXp >= xpForNextLevel) {
                newLevel += 1
            }

            val newData = currentData.copy(
                habits = updatedHabits,
                totalXp = newTotalXp,
                level = newLevel
            )
            dataStoreManager.saveAppData(newData)
        }
    }

    // --- FUNGSI BARU DITAMBAHKAN DI SINI ---
    fun addHabit(name: String, schedule: String, weight: Int) {
        viewModelScope.launch {
            val currentData = appData.value ?: return@launch

            // Membuat ID unik untuk habit baru
            // Cari ID tertinggi, lalu tambahkan 1
            val newHabitId = (currentData.habits.maxOfOrNull { it.id } ?: 0) + 1

            val newHabit = Habit(
                id = newHabitId,
                name = name,
                schedule = schedule,
                weight = weight,
                isCompleted = false
            )

            // Tambahkan habit baru ke daftar yang sudah ada
            val updatedHabits = currentData.habits + newHabit

            val newData = currentData.copy(habits = updatedHabits)
            dataStoreManager.saveAppData(newData)
        }
    }

    fun getXpProgress(): Pair<Int, Int> {
        val data = appData.value ?: return 0 to 100
        val xpForCurrentLevel = (data.level - 1) * 100
        val xpForNextLevel = data.level * 100
        val currentXpInLevel = data.totalXp - xpForCurrentLevel
        return currentXpInLevel to 100
    }
}

