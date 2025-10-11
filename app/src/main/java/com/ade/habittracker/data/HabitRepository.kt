package com.ade.habittracker.data

import android.app.Application
import com.ade.habittracker.model.Achievement
import com.ade.habittracker.model.AppData
import com.ade.habittracker.model.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.json.Json

class HabitRepository(application: Application) {
    private val dataStoreManager = DataStoreManager(application)

    // Menggabungkan dua flow (data utama & tanggal terakhir) menjadi satu sumber data yang utuh
    val appDataFlow: Flow<AppData?> = combine(
        dataStoreManager.appDataFlow,
        dataStoreManager.lastCompletionDateFlow
    ) { appDataJson, lastCompletionDate ->
        if (appDataJson != null) {
            // Gabungkan data JSON dengan data tanggal
            Json.decodeFromString<AppData>(appDataJson).copy(
                lastCompletionDate = lastCompletionDate?.takeIf { it.isNotBlank() }
            )
        } else {
            // Jika tidak ada data sama sekali, berikan data default
            getDefaultAppData()
        }
    }

    // Fungsi untuk menyimpan data, hanya meneruskan ke DataStoreManager
    suspend fun saveAppData(appData: AppData) {
        dataStoreManager.saveAppData(appData)
    }

    private fun getDefaultAppData(): AppData {
        // Data awal saat aplikasi pertama kali dijalankan
        val defaultHabits = listOf(
            Habit(id = 1, name = "Baca Buku 30 Menit", schedule = "Setiap Hari", weight = 50),
            Habit(id = 2, name = "Olahraga Pagi", schedule = "Senin, Rabu, Jumat", weight = 40),
            Habit(id = 3, name = "Belajar Kotlin", schedule = "Setiap Hari", weight = 30, isCompleted = true),
            Habit(id = 4, name = "Minum 8 Gelas Air", schedule = "Setiap Hari", weight = 20, isCompleted = true)
        )
        val defaultAchievements = listOf(
            Achievement(id = 1, title = "Pemula", description = "Menyelesaikan habit pertama kali."),
            Achievement(id = 2, title = "Konsisten", description = "Menyelesaikan habit selama 7 hari berturut-turut."),
            Achievement(id = 3, title = "Master Habit", description = "Mencapai Level 10."),
            Achievement(id = 4, title = "Rajin Belajar", description = "Menyelesaikan habit 'Belajar' 10 kali.")
        )
        return AppData(
            habits = defaultHabits,
            achievements = defaultAchievements,
            totalXp = 80, // Sesuai dengan habit default yang sudah selesai
            level = 1,
            streak = 0,
            lastCompletionDate = null
        )
    }
}

