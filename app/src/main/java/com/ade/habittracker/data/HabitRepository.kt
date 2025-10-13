package com.ade.habittracker.data

import android.content.Context
import com.ade.habittracker.model.Achievement
import com.ade.habittracker.model.AppData
import com.ade.habittracker.model.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class HabitRepository(context: Context) {

    // Memanggil getInstance() untuk memastikan kita hanya punya SATU instance DataStoreManager
    // di seluruh aplikasi. Ini sangat penting untuk mencegah bug.
    private val dataStoreManager = DataStoreManager.getInstance(context.applicationContext)

    // Menggabungkan dua flow (data utama & tanggal terakhir) menjadi satu sumber data yang utuh
    val appData: Flow<AppData?> = combine(
        dataStoreManager.appDataJsonFlow,
        dataStoreManager.lastCompletionDateFlow
    ) { appDataJson, lastCompletionDate ->
        val appData = if (appDataJson != null) {
            // Jika ada data JSON, kita decode.
            Json.decodeFromString<AppData>(appDataJson)
        } else {
            // Jika tidak ada data sama sekali (first run), panggil fungsi getDefaultAppData().
            getDefaultAppData()
        }
        // Selalu perbarui lastCompletionDate di data yang akan dikirim ke ViewModel
        appData.copy(lastCompletionDate = lastCompletionDate)
    }

    // Fungsi untuk menyimpan data, hanya meneruskan ke DataStoreManager
    suspend fun saveAppData(appData: AppData) {
        dataStoreManager.saveAppData(appData)
    }

    // Logika untuk menyediakan data awal saat aplikasi pertama kali dijalankan.
    private fun getDefaultAppData(): AppData {
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
            totalXp = 50, // Dihitung dari habit default yang sudah selesai (30+20)
            level = 1,
            streak = 0,
            lastCompletionDate = null
        )
    }
}

