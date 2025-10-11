package com.ade.habittracker.model

import kotlinx.serialization.Serializable

// Menandakan bahwa class ini bisa diubah jadi JSON dan sebaliknya
@Serializable
data class Habit(
    val id: Int,
    val name: String,
    val schedule: String,
    val weight: Int, // Bobot (XP) sekarang kita sebut weight
    var isCompleted: Boolean = false
)

@Serializable
data class Achievement(
    val id: Int,
    val title: String,
    val description: String,
    var isUnlocked: Boolean = false
)

// Satu objek untuk menyimpan semua data aplikasi
@Serializable
data class AppData(
    val habits: List<Habit> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val totalXp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0
)
