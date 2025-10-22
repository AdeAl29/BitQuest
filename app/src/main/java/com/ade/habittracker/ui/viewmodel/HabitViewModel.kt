package com.ade.habittracker.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ade.habittracker.data.HabitRepository // Repositori Firestore yang baru
import com.ade.habittracker.data.UserData // Import UserData
import com.ade.habittracker.model.AppData
import com.ade.habittracker.model.Habit // Pastikan Habit punya firestoreId: String
import com.ade.habittracker.notification.HabitReminderWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine // Import combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    // Repository sekarang tidak memerlukan context di konstruktornya
    private val repository = HabitRepository()
    private val TAG = "HabitViewModel"

    // --- PERUBAHAN UTAMA: Kombinasi Flow ---
    // Mengambil flow terpisah dari repository
    private val userDataFlow = repository.getUserDataFlow()
    private val habitsFlow = repository.getHabitsFlow()
    // Menggabungkan kedua flow untuk membuat AppData StateFlow
    val appData: StateFlow<AppData?> = combine(userDataFlow, habitsFlow) { userData, habits ->
        userData?.let { user ->
            AppData(
                habits = habits,
                achievements = repository.getDefaultAchievements(), // Ambil dari default (atau Firestore jika ada)
                totalXp = user.totalXp,
                level = user.level,
                streak = user.streak,
                lastResetDate = user.lastResetDate,
                lastCompletionDate = user.lastCompletionDate
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null // Awalnya null sampai data dari Firestore masuk
    )
    // ------------------------------------

    // Flow untuk leaderboard (sama seperti sebelumnya)
    val leaderboardData: StateFlow<List<UserData>> = repository.getLeaderboard().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Logika reset harian (disesuaikan untuk Firestore)
    fun resetHabitsIfNewDay() {
        viewModelScope.launch { // OK: Memanggil suspend dari coroutine scope
            val currentUserData = userDataFlow.stateIn(viewModelScope).value ?: return@launch // Ambil UserData
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date())

            if (currentUserData.lastResetDate != todayStr) {
                Log.d(TAG, "Resetting habits for new day...")
                val currentHabits = habitsFlow.stateIn(viewModelScope).value // Ambil Habits
                var habitsWereReset = false
                currentHabits.forEach { habit ->
                    if (habit.isCompleted) {
                        repository.updateHabitCompletion(habit.firestoreId, false) // Panggil suspend fun
                        habitsWereReset = true
                    }
                }
                // Hanya update lastResetDate jika ada habit yang direset atau belum pernah direset
                if (habitsWereReset || currentUserData.lastResetDate == null) {
                    repository.updateUserStats(mapOf("lastResetDate" to todayStr)) // Panggil suspend fun
                }
                Log.d(TAG, "Habits reset check done.")
            }
        }
    }

    // Fungsi CRUD (disesuaikan untuk Firestore)

    fun addHabit(name: String, schedule: String, weight: Int) {
        viewModelScope.launch { // OK: Memanggil suspend dari coroutine scope
            // Buat objek Habit (UI Model) - ID Int tidak terlalu penting
            val newHabit = Habit(
                firestoreId = "", // Firestore akan generate ID
                name = name,
                schedule = schedule,
                weight = weight
                // isCompleted default false
            )
            repository.addHabit(newHabit) // Panggil suspend fun
        }
    }

    // ID sekarang String
    fun updateHabit(id: String, name: String, schedule: String, weight: Int) {
        viewModelScope.launch { // OK: Memanggil suspend dari coroutine scope
            val currentHabit = appData.value?.habits?.find { it.firestoreId == id }
            if (currentHabit == null) { Log.e(TAG, "Update: Habit $id not found"); return@launch }

            val updatedHabit = Habit( // Buat objek Habit (UI Model)
                firestoreId = id, // Pertahankan ID String
                name = name,
                schedule = schedule,
                weight = weight,
                isCompleted = currentHabit.isCompleted // Jaga status isCompleted
            )
            repository.updateHabit(id, updatedHabit) // Panggil suspend fun
        }
    }

    // ID sekarang String
    fun deleteHabit(id: String) {
        viewModelScope.launch { // OK: Memanggil suspend dari coroutine scope
            val currentData = appData.value
            val habitToDelete = currentData?.habits?.find { it.firestoreId == id }

            // Kurangi XP jika habit yang dihapus sudah selesai
            if (currentData != null && habitToDelete?.isCompleted == true) {
                var newTotalXp = currentData.totalXp - habitToDelete.weight
                if (newTotalXp < 0) newTotalXp = 0
                val newLevel = calculateLevel(newTotalXp)
                // Update XP & Level
                repository.updateUserStats(mapOf("totalXp" to newTotalXp, "level" to newLevel)) // Panggil suspend fun
            }
            // Hapus habit
            repository.deleteHabit(id) // Panggil suspend fun
        }
    }

    // ID sekarang String
    fun toggleHabitCompleted(habitId: String, isCompleted: Boolean) {
        viewModelScope.launch { // OK: Memanggil suspend dari coroutine scope
            val currentData = appData.value ?: return@launch
            val targetHabit = currentData.habits.find { it.firestoreId == habitId }
            if (targetHabit == null) { Log.e(TAG, "Toggle: Habit $habitId not found"); return@launch }

            // 1. Update status isCompleted di Firestore SEBELUM kalkulasi lain
            repository.updateHabitCompletion(habitId, isCompleted) // Panggil suspend fun

            // 2. Kalkulasi perubahan XP & Level (berdasarkan state SEBELUM di-toggle jika membatalkan)
            val xpChange = if (isCompleted) targetHabit.weight else -targetHabit.weight
            var newTotalXp = currentData.totalXp + xpChange
            if (newTotalXp < 0) newTotalXp = 0
            val newLevel = calculateLevel(newTotalXp)

            // Siapkan data untuk update statistik user
            val updates = mutableMapOf<String, Any>(
                "totalXp" to newTotalXp,
                "level" to newLevel
            )

            // 3. Logika Streak, LastCompletionDate, dan Weekly XP (hanya jika isCompleted = true)
            if (isCompleted) {
                val today = Calendar.getInstance(); val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = dateFormat.format(today.time)

                // Update Weekly XP (Tambahkan XP yang baru didapat)
                // Ambil weeklyXp dari UserData (perlu ada di AppData atau query UserData)
                // Kita bisa gunakan FieldValue.increment untuk atomicity
                updates["weeklyXp"] = FieldValue.increment(targetHabit.weight.toDouble()) // Gunakan increment

                // Update Streak & LastCompletionDate jika hari baru
                if (todayStr != currentData.lastCompletionDate) {
                    val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
                    val yesterdayStr = dateFormat.format(yesterday.time)
                    val newStreak = if (currentData.lastCompletionDate == yesterdayStr) currentData.streak + 1 else 1

                    updates["streak"] = newStreak
                    updates["lastCompletionDate"] = todayStr

                    // 4. Cek Achievements (Jalankan hanya jika streak/tanggal berubah)
                    // Fungsi ini dibuat suspend karena mungkin perlu query tambahan
                    checkAndUnlockAchievements(currentData, newTotalXp, newLevel, newStreak)
                } else {
                    // Jika hari sama, cek achievement yang mungkin terbuka hanya karena XP/Level naik
                    checkAndUnlockAchievements(currentData, newTotalXp, newLevel, currentData.streak)
                }
            } else {
                // Jika membatalkan centang, kurangi Weekly XP
                updates["weeklyXp"] = FieldValue.increment(-targetHabit.weight.toDouble()) // Kurangi
                // Logika reset streak jika perlu? (Tergantung desain game Anda)
            }


            // 5. Update semua statistik user di Firestore
            repository.updateUserStats(updates) // Panggil suspend fun
            Log.d(TAG,"User stats updated: $updates")
        }
    }

    // Fungsi cek achievement dibuat suspend
    private suspend fun checkAndUnlockAchievements(currentData: AppData, currentXp: Int, currentLevel: Int, currentStreak: Int) {
        currentData.achievements.forEach { achievement ->
            if (!achievement.isUnlocked) {
                var unlocked = false
                when (achievement.id) {
                    1 -> if (currentXp > 0) unlocked = true // Pemula
                    2 -> if (currentStreak >= 7) unlocked = true // Konsisten
                    3 -> if (currentLevel >= 10) unlocked = true // Master Habit
                    4 -> { // Rajin Belajar
                        // Logika ini perlu implementasi detail
                        Log.w(TAG, "'Rajin Belajar' needs count logic")
                    }
                }
                if (unlocked) {
                    Log.d(TAG, "Achievement unlocked: ${achievement.title}")
                    // TODO: Panggil fungsi repository untuk update status achievement di Firestore jika perlu
                    // repository.updateAchievementStatus(achievement.id, true)
                }
            }
        }
    }

    // Fungsi lain tetap sama
    private fun calculateLevel(totalXp: Int): Int { return (totalXp / 100) + 1 }
    fun getXpProgress(): Pair<Int, Int> {
        val totalXp = appData.value?.totalXp ?: 0
        val currentLevel = appData.value?.level?.takeIf { it > 0 } ?: 1
        val currentLevelXp = (currentLevel - 1) * 100
        val progress = (totalXp - currentLevelXp).coerceAtLeast(0)
        return Pair(progress, 100)
    }
    fun scheduleDailyReminder(context: Context) {
        val reminderRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_habit_reminder", ExistingPeriodicWorkPolicy.KEEP, reminderRequest
        )
    }
}

