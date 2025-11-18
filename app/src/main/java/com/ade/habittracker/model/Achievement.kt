package com.ade.habittracker.model

import kotlinx.serialization.Serializable

@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    // KITA UBAH INI: Dari ImageVector ke Int (Resource ID)
    // Kita hapus @Transient karena Int bisa disimpan ke JSON
    val iconResId: Int,

    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val goal: Int = 1
)