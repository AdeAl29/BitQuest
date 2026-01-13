package com.ade.habittracker.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

// 1. Data Class untuk Misi (Habit)
@Serializable
data class Habit(
    val id: Int,
    val name: String,
    val schedule: String,
    val weight: Int,
    val isCompleted: Boolean = false
)

// 2. Data Class untuk Riwayat Petualangan (History Log)
@Serializable
data class HabitHistoryItem(
    val id: String,         // ID unik (UUID)
    val habitName: String,  // Nama misi yang diselesaikan
    val xpEarned: Int,      // XP yang didapat
    val timestamp: Long     // Waktu penyelesaian (System.currentTimeMillis)
)

// 3. Data Class Utama Aplikasi (AppData)
@Serializable
data class AppData(
    // --- Profil User ---
    val userName: String = "Petualang",
    val userTitle: String = "Petualang Baru",
    val profileImageId: String = "avatar_level1",

    // --- Statistik RPG ---
    val level: Int = 1,
    val totalXp: Int = 0,
    val streak: Int = 0,
    val totalHabitsCompleted: Int = 0,

    // --- Logika Tanggal ---
    val lastCompletionDate: String = "", // Format: yyyy-MM-dd
    val lastResetDate: String = "",      // Format: yyyy-MM-dd

    // --- Daftar Data ---
    val habits: List<Habit> = emptyList(),
    val history: List<HabitHistoryItem> = emptyList() // <-- Field Baru untuk Riwayat
) {
    // Fungsi Helper untuk Simpan ke JSON
    fun toJson(): String {
        val jsonConfig = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        return jsonConfig.encodeToString(this)
    }

    companion object {
        // Fungsi Helper untuk Baca dari JSON
        fun fromJson(jsonString: String): AppData {
            val jsonConfig = Json {
                ignoreUnknownKeys = true // PENTING: Agar tidak crash jika ada field baru/lama beda
                coerceInputValues = true
            }
            return try {
                jsonConfig.decodeFromString(jsonString)
            } catch (e: Exception) {
                // Jika data rusak/kosong, kembalikan data default
                AppData()
            }
        }
    }
}