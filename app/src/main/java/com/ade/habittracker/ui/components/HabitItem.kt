package com.ade.habittracker.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.model.Habit
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.PrimaryColor
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary

@Composable
fun HabitItem(
    habit: Habit,
    onCheckedChanged: (Boolean) -> Unit,
    isMenuExpanded: Boolean,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Logika Visual: Jika selesai, warna agak pudar
    val isDone = habit.isCompleted
    val containerColor = if (isDone) CardBackground.copy(alpha = 0.6f) else CardBackground
    val textColor = if (isDone) TextColorSecondary else TextColorPrimary
    val decoration = if (isDone) TextDecoration.LineThrough else null
    val borderColor = if (isDone) TextColorSecondary.copy(alpha = 0.3f) else Color.Transparent

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        // Border: Jika selesai abu-abu tipis, jika belum tidak ada border (clean look)
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp) // Jarak antar item
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- CHECKBOX (ONE-WAY LOGIC) ---
            Checkbox(
                checked = habit.isCompleted,
                onCheckedChange = onCheckedChanged,
                // 🔥 PENTING: Disable jika sudah selesai (biar gak bisa di-uncheck)
                enabled = !habit.isCompleted,
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryColor,
                    uncheckedColor = TextColorSecondary,
                    // Warna saat disabled (sudah selesai):
                    disabledCheckedColor = PrimaryColor.copy(alpha = 0.5f),
                    checkmarkColor = Color.White
                )
            )

            Spacer(Modifier.width(16.dp))

            // --- TEXT INFO ---
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = decoration // Coret teks jika selesai
                )
                Text(
                    text = habit.schedule,
                    color = TextColorSecondary.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            // --- XP BADGE ---
            // Jika selesai, warna XP jadi abu-abu. Jika belum, kuning menyala.
            Text(
                text = "+${habit.weight} XP",
                color = if (isDone) TextColorSecondary else AccentYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            // --- MENU ---
            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = TextColorSecondary
                    )
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = onDismissMenu,
                    modifier = Modifier.background(CardBackground) // Sesuaikan tema
                ) {
                    // Hanya tampilkan Edit jika BELUM selesai
                    if (!isDone) {
                        DropdownMenuItem(
                            text = { Text("Edit", color = TextColorPrimary) },
                            onClick = {
                                onEditClick()
                                onDismissMenu()
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = { Text("Hapus", color = com.ade.habittracker.ui.theme.ErrorColor) },
                        onClick = {
                            onDeleteClick()
                            onDismissMenu()
                        }
                    )
                }
            }
        }
    }
}