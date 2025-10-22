package com.ade.habittracker.model

// Import kotlinx.serialization TIDAK diperlukan lagi

// Data class untuk Habit (Model yang digunakan di ViewModel dan UI)
data class Habit(
    // ID String dari Firestore, WAJIB ADA.
    val firestoreId: String,
    // ID Integer bisa digunakan untuk UI (misal key di LazyColumn), dibuat dari hashCode.
    // Jika tidak perlu, bisa dihapus.
    val id: Int = firestoreId.hashCode(),
    val name: String,
    val schedule: String,
    val weight: Int,
    val isCompleted: Boolean = false
)

// Data class untuk Achievement (Model UI)
data class Achievement(
    val id: Int, // ID Achievement bisa tetap Int
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false
)

// Data class utama yang MENGGABUNGKAN semua state aplikasi untuk UI
// Data ini dirakit di ViewModel dari sumber data Firestore.
data class AppData(
    val habits: List<Habit> = emptyList(), // Menggunakan model Habit versi baru
    val achievements: List<Achievement> = emptyList(), // List achievement (bisa dari default/Firestore)
    // Statistik ini diambil dari UserData di Firestore
    val totalXp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val lastResetDate: String? = null,
    val lastCompletionDate: String? = null
    // Fungsi toJson/fromJson dihapus
)

