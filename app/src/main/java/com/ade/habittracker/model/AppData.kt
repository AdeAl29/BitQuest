package com.ade.habittracker.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Habit(
    val id: Int,
    val name: String,
    val schedule: String,
    val weight: Int,
    val isCompleted: Boolean = false
)

@Serializable
data class AppData(
    val habits: List<Habit> = emptyList(),
    val totalXp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val lastResetDate: String? = null,
    val lastCompletionDate: String? = null,
    val totalHabitsCompleted: Int = 0,

    // --- PERUBAHAN DI SINI ---
    val userName: String = "Petualang",
    val profileImageId: String = "avatar_level1",
    val userTitle: String = "Petualang Baru" // <-- TAMBAHAN BARU
    // ---------------------
) {
    fun toJson(): String = Json.encodeToString(this)

    companion object {
        fun fromJson(jsonString: String): AppData = Json.decodeFromString(jsonString)
    }
}