package com.ade.habittracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ade.habittracker.model.AppData
import com.ade.habittracker.model.Habit
import com.ade.habittracker.model.Achievement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Membuat instance DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "habit_tracker_prefs")

class DataStoreManager(private val context: Context) {

    // Kunci untuk menyimpan data kita dalam format JSON String
    companion object {
        val APP_DATA_KEY = stringPreferencesKey("app_data")
    }

    // Fungsi untuk mendapatkan data
    val appDataFlow: Flow<AppData> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[APP_DATA_KEY]
        if (jsonString != null) {
            Json.decodeFromString<AppData>(jsonString)
        } else {
            // Jika data belum ada (pertama kali buka), buat data default
            createDefaultData()
        }
    }

    // Fungsi untuk menyimpan data
    suspend fun saveAppData(appData: AppData) {
        val jsonString = Json.encodeToString(appData)
        context.dataStore.edit { preferences ->
            preferences[APP_DATA_KEY] = jsonString
        }
    }

    // Data awal untuk pengguna baru
    private fun createDefaultData(): AppData {
        val defaultHabits = listOf(
            Habit(1, "Baca Buku 30 Menit", "Setiap Hari", 50, false),
            Habit(2, "Olahraga Pagi", "Senin, Rabu, Jumat", 40, false),
            Habit(3, "Belajar Kotlin", "Setiap Hari", 30, false),
            Habit(4, "Minum 8 Gelas Air", "Setiap Hari", 20, false)
        )
        val defaultAchievements = listOf(
            Achievement(1, "Pemula", "Menyelesaikan habit pertama kali.", false),
            Achievement(2, "Konsisten", "Menyelesaikan habit selama 7 hari berturut-turut.", false),
            Achievement(3, "Master Habit", "Mencapai Level 10.", false)
        )
        return AppData(habits = defaultHabits, achievements = defaultAchievements)
    }
}
