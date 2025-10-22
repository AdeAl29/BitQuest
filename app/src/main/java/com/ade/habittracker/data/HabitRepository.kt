package com.ade.habittracker.data

import android.util.Log
import com.ade.habittracker.model.Achievement // Masih perlu untuk default
import com.ade.habittracker.model.AppData
import com.ade.habittracker.model.Habit // Gunakan model Habit yang sudah ada (dengan firestoreId)
import com.ade.habittracker.model.UserData // Pastikan UserData ada

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Exclude

// Definisikan HabitData di sini jika belum ada file terpisah
// Sebaiknya di file HabitData.kt
data class HabitData(
    @get:Exclude var id: String = "", // ID Dokumen Firestore
    val userId: String? = null,        // ID Pengguna
    val name: String = "",
    val schedule: String = "",
    val weight: Int = 0,
    val isCompleted: Boolean = false
) {
    // Konstruktor tanpa argumen diperlukan oleh Firestore
    constructor() : this("", null, "", "", 0, false)
}

class HabitRepository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    // Ambil userId saat ini, bisa null jika belum login
    private val userId: String?
        get() = auth.currentUser?.uid // Gunakan getter agar selalu update
    private val TAG = "HabitRepository"

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAppDataFlow(): Flow<AppData?> = callbackFlow {
        val currentUserId = userId // Ambil userId sekali di awal
        if (currentUserId == null) {
            Log.w(TAG, "User not logged in, cannot fetch app data.")
            trySend(null) // Kirim null jika user belum login
            awaitClose { } // Tutup flow
            return@callbackFlow
        }

        // Listener untuk data pengguna (UserData)
        val userRegistration = db.collection("users").document(currentUserId)
            .addSnapshotListener listener@{ userSnapshot, error ->
                if (handleSnapshotError(error, "User Listener")) {
                    trySend(null) // Kirim null jika ada error
                    return@listener
                }
                // Jika dokumen user belum ada, buat UserData default
                // Penting: Pastikan UserData memiliki konstruktor kosong!
                val userData = userSnapshot?.toObject<UserData>() ?: UserData()

                // Listener untuk koleksi habits milik pengguna
                val habitsRegistration = db.collection("habits").whereEqualTo("userId", currentUserId)
                    .addSnapshotListener habitsListener@{ habitsSnapshot, habitsError ->
                        if (handleSnapshotError(habitsError, "Habits Listener")) {
                            trySend(null) // Kirim null jika ada error
                            return@habitsListener
                        }

                        // Map dokumen Firestore ke List<Habit> (model UI)
                        val habits = habitsSnapshot?.documents?.mapNotNull { doc ->
                            val habitData = doc.toObject<HabitData>()
                            habitData?.let {
                                // Buat objek Habit (UI Model) dengan firestoreId
                                Habit(
                                    firestoreId = doc.id, // ID String dari Firestore
                                    // id = doc.id.hashCode(), // ID Int bisa dibuat otomatis di data class Habit
                                    name = it.name,
                                    schedule = it.schedule,
                                    weight = it.weight,
                                    isCompleted = it.isCompleted
                                )
                            }
                        } ?: emptyList()

                        // Buat objek AppData final untuk dikirim ke ViewModel
                        val appData = AppData(
                            habits = habits,
                            achievements = getDefaultAchievements(), // Ambil dari default (atau Firestore nanti)
                            totalXp = userData.totalXp,
                            level = userData.level,
                            streak = userData.streak,
                            lastResetDate = userData.lastResetDate,
                            lastCompletionDate = userData.lastCompletionDate
                        )
                        Log.d(TAG, "AppData updated and sent via flow.")
                        trySend(appData).isSuccess // Kirim data terbaru ke flow
                    }
                // Saat user listener ditutup, tutup juga habits listener
                awaitClose { habitsRegistration.remove() }
            }
        // Saat flow ditutup (ViewModel dihancurkan), hapus listener user
        awaitClose { userRegistration.remove() }
    }

    // Fungsi helper error
    private fun handleSnapshotError(error: FirebaseFirestoreException?, listenerName: String): Boolean {
        if (error != null) { Log.e(TAG, "Error in $listenerName: ", error); return true }
        return false
    }

    // Default achievements
    private fun getDefaultAchievements(): List<Achievement> {
        return listOf(
            Achievement(id = 1, title = "Pemula", description = "Menyelesaikan habit pertama kali."),
            Achievement(id = 2, title = "Konsisten", description = "Menyelesaikan habit selama 7 hari berturut-turut."),
            Achievement(id = 3, title = "Master Habit", description = "Mencapai Level 10."),
            Achievement(id = 4, title = "Rajin Belajar", description = "Menyelesaikan habit 'Belajar' 10 kali.")
        )
    }

    // --- Fungsi Tulis Data (Semua suspend) ---

    suspend fun addHabit(habit: Habit) { // Terima Habit UI Model
        val currentUserId = userId ?: run { Log.e(TAG, "Add Habit: User null"); return }
        try {
            // Konversi ke HabitData Firestore Model
            val habitData = HabitData(
                userId = currentUserId, name = habit.name, schedule = habit.schedule,
                weight = habit.weight, isCompleted = habit.isCompleted
                // ID akan dibuat otomatis oleh Firestore
            )
            db.collection("habits").add(habitData).await() // OK: suspend fun panggil await
            Log.d(TAG, "Habit added.")
        } catch (e: Exception) { Log.e(TAG, "Error adding habit: ", e) }
    }

    suspend fun updateHabit(habitId: String, updatedHabit: Habit) { // Terima Habit UI Model
        val currentUserId = userId ?: run { Log.e(TAG, "Update Habit: User null"); return }
        try {
            // Konversi ke HabitData Firestore Model
            val habitData = HabitData(
                id = habitId, // Pertahankan ID dokumen Firestore
                userId = currentUserId, name = updatedHabit.name,
                schedule = updatedHabit.schedule, weight = updatedHabit.weight,
                isCompleted = updatedHabit.isCompleted
            )
            db.collection("habits").document(habitId).set(habitData).await() // OK: suspend fun panggil await
            Log.d(TAG, "Habit updated: $habitId")
        } catch (e: Exception) { Log.e(TAG, "Error updating habit $habitId: ", e) }
    }

    suspend fun deleteHabit(habitId: String) {
        userId ?: run { Log.e(TAG, "Delete Habit: User null"); return }
        try {
            db.collection("habits").document(habitId).delete().await() // OK: suspend fun panggil await
            Log.d(TAG, "Habit deleted: $habitId")
        } catch (e: Exception) { Log.e(TAG, "Error deleting habit $habitId: ", e) }
    }

    suspend fun updateHabitCompletion(habitId: String, isCompleted: Boolean) {
        userId ?: run { Log.e(TAG, "Update Completion: User null"); return }
        try {
            db.collection("habits").document(habitId).update("isCompleted", isCompleted).await() // OK: suspend fun panggil await
            Log.d(TAG, "Habit completion updated: $habitId")
        } catch (e: Exception) { Log.e(TAG, "Error updating completion $habitId: ", e) }
    }

    suspend fun updateUserStats(updates: Map<String, Any>) {
        val currentUserId = userId ?: run { Log.e(TAG, "Update Stats: User null"); return }
        try {
            db.collection("users").document(currentUserId).update(updates).await() // OK: suspend fun panggil await
            Log.d(TAG, "User stats updated.")
        } catch (e: Exception) { Log.e(TAG, "Error updating stats: ", e) }
    }

    suspend fun createUserDocument(email: String?) {
        val currentUserId = userId ?: run { Log.e(TAG, "Create Doc: User null"); return }
        // Pastikan UserData memiliki konstruktor kosong
        val initialUserData = UserData(email = email)
        try {
            // Gunakan set dengan merge=true untuk keamanan
            db.collection("users").document(currentUserId).set(initialUserData, SetOptions.merge()).await() // OK: suspend fun panggil await
            Log.d(TAG, "User doc created/merged.")
        } catch (e: Exception) { Log.e(TAG, "Error creating/merging doc: ", e) }
    }

    // Fungsi untuk leaderboard
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLeaderboard(): Flow<List<UserData>> = callbackFlow {
        val currentUserId = userId
        if (currentUserId == null) {
            Log.w(TAG, "Leaderboard: User null"); trySend(emptyList()); awaitClose { }; return@callbackFlow
        }
        val listener = db.collection("users")
            .orderBy("weeklyXp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (handleSnapshotError(error, "Leaderboard Listener")) {
                    trySend(emptyList()); return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { it.toObject<UserData>() } ?: emptyList()
                Log.d(TAG, "Leaderboard updated.")
                trySend(users).isSuccess
            }
        awaitClose { listener.remove() }
    }
}

