package com.ade.habittracker.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Anotasi @Serializable memberitahu library cara mengubah objek ini
@Serializable
data class Habit(
    val id: Int,
    val name: String,
    val schedule: String,
    val weight: Int,
    val isCompleted: Boolean = false
)

@Serializable
data class Achievement(
    val id: Int,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false
)

@Serializable
data class AppData(
    val habits: List<Habit> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val totalXp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    // Kita buat non-serializable agar tidak disimpan dalam JSON utama, karena sudah disimpan terpisah
    @kotlinx.serialization.Transient
    val lastCompletionDate: String? = null
) {
    // --- INI FUNGSI .toJson() YANG HILANG ---
    fun toJson(): String = Json.encodeToString(this)

    companion object {
        // Fungsi untuk membuat objek dari String JSON
        fun fromJson(jsonString: String): AppData = Json.decodeFromString(jsonString)
    }
}

