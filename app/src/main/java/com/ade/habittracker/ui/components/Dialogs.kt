package com.ade.habittracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ade.habittracker.model.Achievement
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.PrimaryColor
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary
import kotlinx.coroutines.delay

@Composable
fun AchievementDescriptionDialog(
    achievement: Achievement,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text(
                text = achievement.title,
                color = if (achievement.isUnlocked) AccentYellow else TextColorPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = achievement.description,
                    color = TextColorSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Progress: ${achievement.progress} / ${achievement.goal}",
                    color = if (achievement.isUnlocked) AccentYellow else TextColorPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Tutup", color = PrimaryColor)
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    habitName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hapus Misi?", color = TextColorPrimary) },
        text = { Text("Apakah Anda yakin ingin menghapus misi \"$habitName\"?", color = TextColorSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Hapus", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = PrimaryColor)
            }
        },
        containerColor = CardBackground
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentName) }
    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = { Text("Ubah Nama Profil", color = TextColorPrimary) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Nama Baru") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = PrimaryColor,
                    unfocusedIndicatorColor = TextColorSecondary,
                    cursorColor = PrimaryColor,
                    focusedTextColor = TextColorPrimary,
                    unfocusedTextColor = TextColorPrimary,
                    focusedLabelColor = PrimaryColor,
                    unfocusedLabelColor = TextColorSecondary
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(text)
                    onDismiss()
                },
                enabled = text.isNotBlank()
            ) {
                Text("Simpan", color = PrimaryColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextColorSecondary)
            }
        }
    )

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }
}