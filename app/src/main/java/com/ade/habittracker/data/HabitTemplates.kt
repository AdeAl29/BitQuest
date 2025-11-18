package com.ade.habittracker.data

import com.ade.habittracker.model.HabitTemplate

// Daftar template misi (NILAI XP DISESUAIKAN DENGAN NILAI DASAR 10)
val predefinedHabitTemplates = listOf(

    // Kategori: Bobot Ringan (wi = 1x -> 10 XP)
    "Tugas Ringan (10 XP)" to listOf(
        HabitTemplate("Minum 1L air", "Setiap Hari", 10),
        HabitTemplate("Merapikan tempat tidur", "Setiap Pagi", 10),
        HabitTemplate("Sarapan sehat", "Setiap Pagi", 10),
        HabitTemplate("Meditasi 5-10 menit", "Setiap Hari", 10),
        HabitTemplate("Stretching / Peregangan 5 menit", "Setiap Hari", 10),
        HabitTemplate("Menyiapkan pakaian untuk besok", "Setiap Malam", 10),
        HabitTemplate("Menyiram tanaman", "Setiap Hari", 10),
        HabitTemplate("Membuang sampah", "Setiap Hari", 10)
    ),

    // Kategori: Bobot Sedang (wi = 3x -> 30 XP)
    "Tugas Sedang (30 XP)" to listOf(
        HabitTemplate("Olahraga ringan 20-30 menit", "Setiap Hari", 30),
        HabitTemplate("Membaca buku (non-akademik) 20 menit", "Setiap Hari", 30),
        HabitTemplate("Review catatan kuliah", "Setiap Hari", 30),
        HabitTemplate("Belajar bahasa baru 15 menit", "Setiap Hari", 30),
        HabitTemplate("Menulis jurnal harian", "Setiap Malam", 30),
        HabitTemplate("Tidak makan junk food", "Setiap Hari", 30),
        HabitTemplate("Membersihkan/Merapikan kamar", "Setiap Hari", 30),
        HabitTemplate("Tidur 7-8 jam", "Setiap Malam", 30)
    ),

    // Kategori: Bobot Berat (wi = 5x -> 50 XP)
    "Tugas Berat (50 XP)" to listOf(
        HabitTemplate("Mengerjakan Skripsi (Sesi Fokus 45 menit)", "Setiap Hari", 50),
        HabitTemplate("Latihan coding 1 jam", "Setiap Hari", 50),
        HabitTemplate("Belajar materi kuliah baru 1 jam", "Setiap Hari", 50),
        HabitTemplate("Menyelesaikan tugas utama/prioritas", "Setiap Hari", 50),
        HabitTemplate("Olahraga berat (Gym/Lari)", "3x Seminggu", 50),
        HabitTemplate("Tidak merokok", "Setiap Hari", 50),
        HabitTemplate("Tidak main game/medsos di jam produktif", "Setiap Hari", 50)
    ),

    // Kategori: Side Quest (Bobot Bervariasi)
    "Tantangan (Side Quest)" to listOf(
        HabitTemplate("Telepon orang tua / keluarga", "Tugas Sekali", 20),
        HabitTemplate("Mencoba resep masakan baru", "Tugas Sekali", 40),
        HabitTemplate("Hubungi Teman Lama", "Tugas Sekali", 30),
        HabitTemplate("Jalan-jalan tanpa tujuan (refreshing)", "Tugas Sekali", 50),
        HabitTemplate("Donasi atau bantu teman", "Tugas Sekali", 50),
        HabitTemplate("Kunjungi Tempat Baru", "Tugas Sekali", 50),
        HabitTemplate("24 Jam Tanpa Keluhan", "Tugas Sekali", 100) // Quest spesial
    )
)