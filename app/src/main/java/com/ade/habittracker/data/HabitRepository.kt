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

// Inisialisasi DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "habit_tracker_prefs")

class HabitRepository(private val context: Context) {

    private val APP_DATA_KEY = stringPreferencesKey("app_data_json")

    val appData: Flow<AppData?> = context.dataStore.data
        .map { preferences ->
            val jsonString = preferences[APP_DATA_KEY]
            if (jsonString != null) {
                AppData.fromJson(jsonString)
            } else {
                AppData() // Buat AppData default
            }
        }

    suspend fun saveAppData(appData: AppData) {
        val jsonString = appData.toJson()
        context.dataStore.edit { preferences ->
            preferences[APP_DATA_KEY] = jsonString
        }
    }
}