package com.ade.habittracker.ui.components.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background // <-- TAMBAHAN IMPORT DI SINI
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
// ... sisa import ...
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.model.Habit
import com.ade.habittracker.model.HabitTemplate
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.ErrorColor
import com.ade.habittracker.ui.theme.PrimaryColor
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary
// ... (sisa kode AddOptionsSheet, TemplateHabitSheet, HabitTemplateItem tetap sama)
// --- Data class untuk pilihan bobot ---
private data class BobotOption(val label: String, val xp: Int)

// --- PERUBAHAN DI SINI ---
private val bobotOptions = listOf(
    BobotOption("Ringan (1x)", 10),   // Diubah dari 100
    BobotOption("Sedang (3x)", 30),   // Diubah dari 300
    BobotOption("Berat (5x)", 50)    // Diubah dari 500
)
// -------------------------

@Composable
fun AddOptionsSheet(
    onManualAddClick: () -> Unit,
    onTemplateAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tambah Misi",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextColorPrimary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = onTemplateAddClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text("Pilih dari Template", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onManualAddClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, PrimaryColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text("Buat Misi Sendiri (Manual)", color = PrimaryColor, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TemplateHabitSheet(
    templates: List<Pair<String, List<HabitTemplate>>>,
    onTemplateClick: (HabitTemplate) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "Pilih Misi dari Template",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextColorPrimary,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        templates.forEach { (category, habitList) ->
            item {
                Text(
                    text = category.uppercase(),
                    color = TextColorSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(habitList, key = { it.name }) { template ->
                HabitTemplateItem(
                    template = template,
                    onClick = { onTemplateClick(template) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTemplateItem(
    template: HabitTemplate,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(template.name, color = TextColorPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(template.schedule, color = TextColorSecondary, fontSize = 12.sp)
            }
            Spacer(Modifier.width(16.dp))
            Text(
                "+${template.weight} XP",
                color = AccentYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAddHabitSheet(
    habitToEdit: Habit?,
    onConfirm: (String, String, Int) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var schedule by remember { mutableStateOf("") }
    // --- PERUBAHAN DI SINI ---
    // Simpan 'BobotOption' yang dipilih, default ke "Ringan"
    var selectedBobot by remember { mutableStateOf(bobotOptions[0]) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    // -------------------------

    LaunchedEffect(habitToEdit) {
        if (habitToEdit != null) {
            name = habitToEdit.name
            schedule = habitToEdit.schedule
            // Cari BobotOption berdasarkan XP yang disimpan
            selectedBobot = bobotOptions.find { it.xp == habitToEdit.weight } ?: bobotOptions[0]
        } else {
            name = ""
            schedule = ""
            selectedBobot = bobotOptions[0]
        }
    }

    // Form valid jika nama dan jadwal tidak kosong
    val isFormValid = name.isNotBlank() && schedule.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (habitToEdit == null) "Tambah Misi Manual" else "Edit Misi",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextColorPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = name,
            onValueChange = { newName -> name = newName },
            label = { Text("Nama Misi") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
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
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = schedule,
            onValueChange = { newSchedule -> schedule = newSchedule },
            label = { Text("Jadwal (cth: Setiap Hari, Senin)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
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
        Spacer(Modifier.height(8.dp))

        // --- INPUT BOBOT DIUBAH MENJADI DROPDOWN ---
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = "${selectedBobot.label} (+${selectedBobot.xp} XP)", // Tampilkan label & XP
                onValueChange = { }, // Biarkan kosong
                label = { Text("Pilih Bobot (wi)") },
                readOnly = true, // Buat tidak bisa diketik
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, "Pilih Bobot", modifier = Modifier.clickable { isDropdownExpanded = true })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isDropdownExpanded = true }, // Klik di mana saja untuk buka
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

            // Menu Dropdown
            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Sesuaikan lebar dropdown
                    .background(CardBackground) // <-- Baris ini sekarang sudah benar
            ) {
                bobotOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "${option.label} (+${option.xp} XP)",
                                color = if (option.xp == selectedBobot.xp) AccentYellow else TextColorPrimary
                            )
                        },
                        onClick = {
                            selectedBobot = option
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }
        // --- AKHIR PERUBAHAN INPUT BOBOT ---

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = TextColorSecondary)
            ) {
                Text("Batal")
            }
            Button(
                // Kirim XP yang dipilih (100, 300, atau 500)
                onClick = { onConfirm(name, schedule, selectedBobot.xp) },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text(if (habitToEdit == null) "Tambah Misi" else "Simpan Perubahan")
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}