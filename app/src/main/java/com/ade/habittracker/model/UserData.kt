package com.ade.habittracker.model

/**
 * Data class ini merepresentasikan struktur data Pengguna (User)
 * SEPERTI yang disimpan di Cloud Firestore, dalam collection 'users'.
 */
data class UserData(
    // Informasi dasar pengguna (opsional bisa ditambah nama, dll)
    val email: String? = null,

    // Statistik Gamifikasi
    val totalXp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,

    // Data untuk fitur reset harian & streak
    val lastResetDate: String? = null,
    val lastCompletionDate: String? = null,

    // Data untuk fitur leaderboard mingguan
    val weeklyXp: Int = 0
) {
    // Konstruktor tanpa argumen diperlukan oleh Firestore untuk deserialisasi
    constructor() : this(null, 0, 1, 0, null, null, 0)
}

