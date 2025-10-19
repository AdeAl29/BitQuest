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
    // --- DI SINI PERUBAHANNYA ---
    // Properti baru ditambahkan untuk melacak tanggal reset terakhir
    val lastResetDate: String? = null,
    // ----------------------------
    @kotlinx.serialization.Transient
    val lastCompletionDate: String? = null
) {
    fun toJson(): String = Json.encodeToString(this)

    companion object {
        fun fromJson(jsonString: String): AppData = Json.decodeFromString(jsonString)
    }
}

