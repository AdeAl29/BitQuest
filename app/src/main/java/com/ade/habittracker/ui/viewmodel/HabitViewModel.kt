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

    // StateFlow untuk menampung semua data aplikasi
    private val _appData = MutableStateFlow<AppData?>(null)
    val appData: StateFlow<AppData?> = _appData.asStateFlow()

    init {
        // Saat ViewModel dibuat, langsung ambil data dari DataStore
        viewModelScope.launch {
            dataStoreManager.appDataFlow.collect { data ->
                _appData.value = data
            }
        }
    }

    // --- LOGIKA UTAMA ADA DI SINI ---
    fun toggleHabitCompleted(habitId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            // Ambil data saat ini
            val currentData = appData.value ?: return@launch

            var newTotalXp = currentData.totalXp
            val habit = currentData.habits.find { it.id == habitId } ?: return@launch

            // Algoritma Weighted Scoring
            if (isCompleted) {
                newTotalXp += habit.weight // Tambah XP
            } else {
                newTotalXp -= habit.weight // Kurangi XP (jika batal)
            }
            if (newTotalXp < 0) newTotalXp = 0 // XP tidak bisa minus

            // Update status habit
            val updatedHabits = currentData.habits.map {
                if (it.id == habitId) {
                    it.copy(isCompleted = isCompleted)
                } else {
                    it
                }
            }

            // Cek Kenaikan Level (Gamification)
            // Setiap 100 poin naik 1 level
            var newLevel = currentData.level
            val xpForNextLevel = newLevel * 100
            if (newTotalXp >= xpForNextLevel) {
                newLevel += 1
                // Di sini bisa tambahkan logika notifikasi "Level Up!"
            }

            // TODO: Cek Streak dan Achievement (diimplementasikan nanti)

            // Buat objek data baru dengan semua perubahan
            val newData = currentData.copy(
                habits = updatedHabits,
                totalXp = newTotalXp,
                level = newLevel
            )

            // Simpan data baru ke DataStore
            dataStoreManager.saveAppData(newData)
        }
    }

    // Fungsi untuk mendapatkan XP progress untuk level saat ini
    fun getXpProgress(): Pair<Int, Int> {
        val data = appData.value ?: return 0 to 100
        val xpForCurrentLevel = (data.level - 1) * 100
        val xpForNextLevel = data.level * 100
        val currentXpInLevel = data.totalXp - xpForCurrentLevel
        return currentXpInLevel to 100 // Total XP untuk naik level selalu 100
    }
}
