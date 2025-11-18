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
    val borderColor = if (habit.isCompleted) PrimaryColor else CardBackground
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(2.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = habit.isCompleted,
                onCheckedChange = onCheckedChanged,
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryColor,
                    uncheckedColor = TextColorSecondary,
                    checkmarkColor = Color.White
                )
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(habit.name, color = TextColorPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(habit.schedule, color = TextColorSecondary, fontSize = 14.sp)
            }
            Spacer(Modifier.width(16.dp))
            Text(
                "+${habit.weight}\nXP",
                color = AccentYellow,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp,
                fontSize = 16.sp
            )
            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = TextColorSecondary)
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = onDismissMenu,
                    modifier = Modifier.background(Color(0xFF48484A))
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit", color = TextColorPrimary) },
                        onClick = {
                            onEditClick()
                            onDismissMenu()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus", color = Color.Red) },
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