// (File: model/Achievement.kt)
package com.ade.habittracker.model

import androidx.compose.material.icons.Icons // <-- TAMBAHKAN IMPORT INI
import androidx.compose.material.icons.filled.Star // <-- TAMBAHKAN IMPORT INI
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,

    @Transient
    val icon: ImageVector = Icons.Default.Star, // <-- PERBAIKAN: Beri nilai default

    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val goal: Int = 1
)