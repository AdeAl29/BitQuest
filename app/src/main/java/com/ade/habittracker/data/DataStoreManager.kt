package com.ade.habittracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ade.habittracker.model.AppData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Inisialisasi DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "habit_tracker_prefs")

class DataStoreManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        private val APP_DATA_KEY = stringPreferencesKey("app_data")
        // --- BUAT KEY BARU UNTUK TANGGAL ---
        private val LAST_COMPLETION_DATE_KEY = stringPreferencesKey("last_completion_date")
    }

    // Fungsi untuk menyimpan seluruh data aplikasi
    suspend fun saveAppData(appData: AppData) {
        dataStore.edit { preferences ->
            // Simpan data utama sebagai JSON
            val appDataJson = Json.encodeToString(
                appData.copy(lastCompletionDate = null) // Jangan simpan tanggal di dalam JSON
            )
            preferences[APP_DATA_KEY] = appDataJson

            // --- SIMPAN TANGGAL SECARA TERPISAH ---
            preferences[LAST_COMPLETION_DATE_KEY] = appData.lastCompletionDate ?: ""
        }
    }

    // Flow untuk mendapatkan data aplikasi secara real-time
    val appDataFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[APP_DATA_KEY]
    }

    // --- BUAT FLOW BARU UNTUK MENDAPATKAN TANGGAL ---
    val lastCompletionDateFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[LAST_COMPLETION_DATE_KEY]
    }
}

