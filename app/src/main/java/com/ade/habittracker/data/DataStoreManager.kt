package com.ade.habittracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ade.habittracker.model.AppData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Membuat instance DataStore sebagai singleton (hanya ada satu di seluruh aplikasi)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "habit_tracker_preferences")

class DataStoreManager private constructor(private val context: Context) {

    // Kunci-kunci untuk menyimpan data di dalam file DataStore
    private val APP_DATA_JSON_KEY = stringPreferencesKey("app_data_json")
    private val LAST_COMPLETION_DATE_KEY = stringPreferencesKey("last_completion_date")

    // Flow untuk "mengalirkan" data utama (dalam format JSON) dari DataStore ke Repository
    val appDataJsonFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            // Tangani error jika file tidak bisa dibaca
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // Ambil data string berdasarkan kuncinya
            preferences[APP_DATA_JSON_KEY]
        }

    // Flow untuk "mengalirkan" data tanggal terakhir dari DataStore ke Repository
    val lastCompletionDateFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[LAST_COMPLETION_DATE_KEY]
        }


    // Fungsi untuk menyimpan seluruh data aplikasi ke DataStore
    suspend fun saveAppData(appData: AppData) {
        context.dataStore.edit { preferences ->
            // Panggilan .toJson() ini sekarang akan berhasil karena sudah kita definisikan di AppData.kt
            preferences[APP_DATA_JSON_KEY] = appData.toJson()
            preferences[LAST_COMPLETION_DATE_KEY] = appData.lastCompletionDate ?: ""
        }
    }

    // companion object digunakan untuk membuat instance singleton
    companion object {
        @Volatile
        private var INSTANCE: DataStoreManager? = null

        // Fungsi ini memastikan kita selalu menggunakan objek DataStoreManager yang sama
        fun getInstance(context: Context): DataStoreManager {
            return INSTANCE ?: synchronized(this) {
                val instance = DataStoreManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

