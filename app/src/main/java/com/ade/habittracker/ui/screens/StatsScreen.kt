package com.ade.habittracker.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ade.habittracker.model.HabitHistoryItem
import com.ade.habittracker.ui.components.FallingSnowEffect
import com.ade.habittracker.ui.theme.*
import com.ade.habittracker.ui.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen(
    level: Int,
    streak: Int,
    totalXp: Int,
    xpProgress: Int,
    maxXp: Int,
    // 🔥 PARAMETER BARU: TOTAL LOGIN 🔥
    totalLoginDays: Int,
    userName: String,
    userTitle: String,
    @DrawableRes profileImageResId: Int,
    onScheduleReminderClick: () -> Unit,
    onNameClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onTitleClick: () -> Unit,
    // ViewModel diperlukan untuk mengambil data history
    viewModel: HabitViewModel? = null
) {
    // --- STATE MANAGEMENT ---
    // Mengambil data history dari ViewModel (jika ada)
    val historyList = viewModel?.habitHistory?.collectAsStateWithLifecycle()?.value ?: emptyList()

    // State untuk menampilkan/menyembunyikan Dialog History
    var showHistoryDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        // ❄️ Background Salju
        FallingSnowEffect(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .zIndex(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- HEADER TITLE ---
            Text(
                text = "STATUS KARAKTER",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = AccentYellow,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // ─── HERO PROFILE CARD ───
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // AVATAR DENGAN LEVEL BADGE
                    Box(contentAlignment = Alignment.BottomCenter) {
                        Image(
                            painter = painterResource(id = profileImageResId),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(3.dp, AccentYellow, CircleShape)
                                .clickable { onAvatarClick() }
                        )

                        // Badge Level Melayang
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryColor,
                            modifier = Modifier.offset(y = 12.dp),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = "LVL $level",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // NAMA & GELAR
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userName,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextColorPrimary
                        )
                        IconButton(onClick = onNameClick, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Edit, "Edit", tint = TextColorSecondary)
                        }
                    }

                    Text(
                        text = userTitle,
                        fontSize = 16.sp,
                        color = AccentYellow,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { onTitleClick() }
                            .padding(4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // XP BAR CUSTOM
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("EXP Progress", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextColorSecondary)
                            Text("$xpProgress / $maxXp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextColorPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { xpProgress.toFloat() / maxXp.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = AccentYellow,
                            trackColor = Color(0xFF48484A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─── GRID STATISTIK RPG ───
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // 1. KARTU STREAK
                item {
                    GameStatCard(
                        title = "Streak",
                        value = "$streak Hari",
                        icon = Icons.Default.LocalFireDepartment,
                        iconColor = Color(0xFFFF5722), // Merah Api
                        subText = "Konsistensi"
                    )
                }

                // 2. KARTU TOTAL XP (KLIK UNTUK HISTORY)
                item {
                    GameStatCard(
                        title = "Total XP",
                        value = "$totalXp XP",
                        icon = Icons.Default.Star,
                        iconColor = AccentYellow,
                        subText = "Klik utk Riwayat 📜",
                        isClickable = true,
                        onClick = { showHistoryDialog = true }
                    )
                }

                // 3. KARTU TOTAL MISI
                item {
                    GameStatCard(
                        title = "Misi Selesai",
                        value = "${historyList.size}",
                        icon = Icons.Default.CheckCircle,
                        iconColor = Color(0xFF4CAF50), // Hijau
                        subText = "Total Kontribusi"
                    )
                }

                // 4. 🔥 KARTU TOTAL LOGIN (BARU) 🔥
                item {
                    GameStatCard(
                        title = "Total Login",
                        value = "$totalLoginDays Hari",
                        icon = Icons.Default.DateRange, // Icon Kalender
                        iconColor = Color(0xFF2196F3), // Biru Langit
                        subText = "Dedikasi Waktu"
                    )
                }
            }

            // BUTTON NOTIFIKASI
            Button(
                onClick = onScheduleReminderClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground.copy(alpha = 0.7f)),
                border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Icon(Icons.Default.Notifications, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Aktifkan Notifikasi Harian", color = PrimaryColor, fontSize = 14.sp)
            }
        }

        // ─── DIALOG POP-UP HISTORY ───
        if (showHistoryDialog) {
            HistoryLogDialog(
                historyList = historyList,
                onDismiss = { showHistoryDialog = false }
            )
        }
    }
}

// ─── KOMPONEN: KARTU STATISTIK (GAME STYLE) ───
@Composable
fun GameStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    subText: String,
    isClickable: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        // Beri border tipis jika item bisa diklik
        border = if (isClickable) BorderStroke(1.dp, AccentYellow.copy(alpha = 0.5f)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable(enabled = isClickable) { onClick?.invoke() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Header: Icon + Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, color = TextColorSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Value Besar
            Text(value, color = TextColorPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            // Subtext Kecil
            Text(subText, color = TextColorSecondary.copy(alpha = 0.6f), fontSize = 10.sp)
        }
    }
}

// ─── KOMPONEN: DIALOG RIWAYAT (HISTORY LOG) ───
@Composable
fun HistoryLogDialog(
    historyList: List<HabitHistoryItem>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp) // Maksimal tinggi pop-up
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Header Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "RIWAYAT PETUALANGAN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentYellow
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Tutup", tint = TextColorSecondary)
                    }
                }

                Divider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                // List Konten
                if (historyList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada jejak petualangan...", color = TextColorSecondary, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(historyList) { item ->
                            HistoryItemRow(item)
                        }
                    }
                }
            }
        }
    }
}

// ─── KOMPONEN: BARIS ITEM HISTORY ───
@Composable
fun HistoryItemRow(item: HabitHistoryItem) {
    // Format Tanggal: Contoh "12 Jan, 14:30"
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID"))
    val dateStr = dateFormat.format(Date(item.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2C2C2E), RoundedCornerShape(12.dp)) // Latar agak gelap
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ikon Check
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(PrimaryColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, null, tint = PrimaryColor, modifier = Modifier.size(16.dp))
        }

        Spacer(Modifier.width(12.dp))

        // Nama Misi & Tanggal
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.habitName,
                color = TextColorPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = dateStr,
                color = TextColorSecondary,
                fontSize = 12.sp
            )
        }

        // Badge XP
        Surface(
            color = AccentYellow.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "+${item.xpEarned} XP",
                color = AccentYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}