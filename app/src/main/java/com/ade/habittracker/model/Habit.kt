package com.ade.habittracker.model

/**
 * Model data Habit yang digunakan di seluruh UI (ViewModel, Screen, dll).
 * Berisi ID dari Firestore untuk operasi update/delete.
 */
data class Habit(
    val firestoreId: String = "", // ID Dokumen dari Firestore
    val name: String = "",
    val schedule: String = "", // Contoh: "Senin, Rabu, Jumat"
    val weight: Int = 10, // XP yang didapat dari habit ini
    val isCompleted: Boolean = false
)