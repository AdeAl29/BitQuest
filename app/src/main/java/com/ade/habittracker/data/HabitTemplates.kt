package com.ade.habittracker.data

import com.ade.habittracker.model.HabitTemplate

// Daftar template misi (SETIAP KATEGORI 20 ITEM)
val predefinedHabitTemplates = listOf(

    // ======================
    // TUGAS RINGAN (10 XP)
    // ======================
    "Tugas Ringan (10 XP)" to listOf(
        HabitTemplate("Minum 1L air", "Setiap Hari", 10),
        HabitTemplate("Merapikan tempat tidur", "Setiap Pagi", 10),
        HabitTemplate("Sarapan sehat", "Setiap Pagi", 10),
        HabitTemplate("Meditasi 5-10 menit", "Setiap Hari", 10),
        HabitTemplate("Stretching 5 menit", "Setiap Hari", 10),
        HabitTemplate("Menyiapkan pakaian besok", "Setiap Malam", 10),
        HabitTemplate("Menyiram tanaman", "Setiap Hari", 10),
        HabitTemplate("Membuang sampah", "Setiap Hari", 10),
        HabitTemplate("Merapikan meja belajar", "Setiap Hari", 10),
        HabitTemplate("Mencuci botol minum", "Setiap Hari", 10),
        HabitTemplate("Membaca kutipan motivasi", "Setiap Hari", 10),
        HabitTemplate("Menulis to-do list", "Setiap Pagi", 10),
        HabitTemplate("Membersihkan notifikasi HP", "Setiap Hari", 10),
        HabitTemplate("Mengatur alarm besok", "Setiap Malam", 10),
        HabitTemplate("Cuci tangan sebelum makan", "Setiap Hari", 10),
        HabitTemplate("Rapikan tas", "Setiap Hari", 10),
        HabitTemplate("Minum vitamin", "Setiap Hari", 10),
        HabitTemplate("Tarik napas dalam 3x", "Setiap Hari", 10),
        HabitTemplate("Senyum ke diri sendiri", "Setiap Hari", 10),
        HabitTemplate("Tidur tepat waktu", "Setiap Malam", 10)
    ),

    // ======================
    // TUGAS SEDANG (30 XP)
    // ======================
    "Tugas Sedang (30 XP)" to listOf(
        HabitTemplate("Olahraga ringan 20 menit", "Setiap Hari", 30),
        HabitTemplate("Membaca buku 20 menit", "Setiap Hari", 30),
        HabitTemplate("Review catatan kuliah", "Setiap Hari", 30),
        HabitTemplate("Belajar bahasa baru 15 menit", "Setiap Hari", 30),
        HabitTemplate("Menulis jurnal harian", "Setiap Malam", 30),
        HabitTemplate("Tidak makan junk food", "Setiap Hari", 30),
        HabitTemplate("Membersihkan kamar", "Setiap Hari", 30),
        HabitTemplate("Tidur 7-8 jam", "Setiap Malam", 30),
        HabitTemplate("Latihan soal kuliah", "Setiap Hari", 30),
        HabitTemplate("Meringkas materi", "Setiap Hari", 30),
        HabitTemplate("Berjalan kaki 5.000 langkah", "Setiap Hari", 30),
        HabitTemplate("Membaca artikel edukatif", "Setiap Hari", 30),
        HabitTemplate("Belajar mandiri 30 menit", "Setiap Hari", 30),
        HabitTemplate("Mengurangi gula", "Setiap Hari", 30),
        HabitTemplate("Berjemur pagi", "Setiap Pagi", 30),
        HabitTemplate("Merapikan file laptop", "Setiap Hari", 30),
        HabitTemplate("Latihan fokus (Pomodoro)", "Setiap Hari", 30),
        HabitTemplate("Menulis refleksi harian", "Setiap Malam", 30),
        HabitTemplate("Berdoa / refleksi diri", "Setiap Hari", 30),
        HabitTemplate("No medsos 1 jam", "Setiap Hari", 30)
    ),

    // ======================
    // TUGAS BERAT (50 XP)
    // ======================
    "Tugas Berat (50 XP)" to listOf(
        HabitTemplate("Skripsi fokus 45 menit", "Setiap Hari", 50),
        HabitTemplate("Latihan coding 1 jam", "Setiap Hari", 50),
        HabitTemplate("Belajar materi baru 1 jam", "Setiap Hari", 50),
        HabitTemplate("Menyelesaikan tugas prioritas", "Setiap Hari", 50),
        HabitTemplate("Gym / lari intens", "3x Seminggu", 50),
        HabitTemplate("Tidak merokok", "Setiap Hari", 50),
        HabitTemplate("No game saat jam produktif", "Setiap Hari", 50),
        HabitTemplate("Presentasi / latihan public speaking", "Setiap Hari", 50),
        HabitTemplate("Mengerjakan project pribadi", "Setiap Hari", 50),
        HabitTemplate("Menyelesaikan 1 modul kursus", "Setiap Hari", 50),
        HabitTemplate("Deep work 90 menit", "Setiap Hari", 50),
        HabitTemplate("Menulis laporan panjang", "Setiap Hari", 50),
        HabitTemplate("Latihan algoritma", "Setiap Hari", 50),
        HabitTemplate("Belajar untuk ujian", "Setiap Hari", 50),
        HabitTemplate("Mengurangi distraksi total", "Setiap Hari", 50),
        HabitTemplate("Latihan logika 1 jam", "Setiap Hari", 50),
        HabitTemplate("Review target mingguan", "Setiap Hari", 50),
        HabitTemplate("Menulis artikel panjang", "Setiap Hari", 50),
        HabitTemplate("Belajar mandiri 2 jam", "Setiap Hari", 50),
        HabitTemplate("Menyelesaikan milestone besar", "Setiap Hari", 50)
    ),

    // ======================
    // SIDE QUEST (VARIATIF)
    // ======================
    "Tantangan (Side Quest)" to listOf(
        HabitTemplate("Telepon orang tua", "Tugas Sekali", 20),
        HabitTemplate("Mencoba resep baru", "Tugas Sekali", 40),
        HabitTemplate("Hubungi teman lama", "Tugas Sekali", 30),
        HabitTemplate("Jalan-jalan tanpa tujuan", "Tugas Sekali", 50),
        HabitTemplate("Donasi / bantu orang", "Tugas Sekali", 50),
        HabitTemplate("Kunjungi tempat baru", "Tugas Sekali", 50),
        HabitTemplate("Unduh dan gunakan Duolingo", "Tugas Sekali", 30),
        HabitTemplate("24 jam tanpa keluhan", "Tugas Sekali", 100),
        HabitTemplate("Menonton dokumenter", "Tugas Sekali", 30),
        HabitTemplate("Membersihkan galeri HP", "Tugas Sekali", 20),
        HabitTemplate("Uninstall aplikasi tidak penting", "Tugas Sekali", 20),
        HabitTemplate("Baca 1 buku selesai", "Tugas Sekali", 80),
        HabitTemplate("Ikut webinar / seminar", "Tugas Sekali", 50),
        HabitTemplate("Menulis surat untuk diri sendiri", "Tugas Sekali", 30),
        HabitTemplate("Digital detox 1 hari", "Tugas Sekali", 70),
        HabitTemplate("Bangun sebelum jam 5", "Tugas Sekali", 40),
        HabitTemplate("No gula seharian", "Tugas Sekali", 40),
        HabitTemplate("Berani bilang tidak", "Tugas Sekali", 30),
        HabitTemplate("Berbuat baik tanpa alasan", "Tugas Sekali", 50),
        HabitTemplate("Menyusun rencana hidup", "Tugas Sekali", 100)
    )
)
