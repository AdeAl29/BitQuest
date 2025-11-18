package com.ade.habittracker.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
// import androidx.compose.foundation.layout.offset // 'offset' diganti 'Spacer'
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit // <-- IMPORT BARU
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton // <-- IMPORT BARU
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Hapus 'import com.ade.habittracker.R' jika ada
import com.ade.habittracker.ui.components.StatCard
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.PrimaryColor
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary

@Composable
fun StatsScreen(
    level: Int,
    streak: Int,
    totalXp: Int,
    xpProgress: Int,
    maxXp: Int,
    userName: String,
    userTitle: String,
    @DrawableRes profileImageResId: Int,
    onScheduleReminderClick: () -> Unit,
    onNameClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onTitleClick: () -> Unit // <-- PARAMETER BARU
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "PROFIL & STATS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextColorPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- Kartu Profil ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = profileImageResId),
                    contentDescription = "Foto Profil Pengguna",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .border(BorderStroke(2.dp, PrimaryColor), CircleShape)
                        .clickable { onAvatarClick() }
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f), // Kolom teks mengambil sisa ruang
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // --- PERUBAHAN DI SINI: Baris untuk Nama dan Ikon Pensil ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = userName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextColorPrimary,
                            modifier = Modifier.weight(1f) // Nama mengambil sisa ruang
                        )
                        // Ikon Pensil di ujung kanan
                        IconButton(
                            onClick = onNameClick, // Aksi klik pindah ke sini
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Ubah Nama",
                                tint = TextColorSecondary
                            )
                        }
                    }
                    // --- AKHIR PERUBAHAN ---

                    // --- PERUBAHAN DI SINI: Jarak dan Gelar Klik ---
                    Spacer(modifier = Modifier.height(4.dp)) // Menambah jarak

                    Text(
                        text = userTitle,
                        fontSize = 16.sp,
                        color = AccentYellow,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onTitleClick() } // Gelar sekarang bisa diklik
                            .padding(vertical = 2.dp)
                    )
                    // --- AKHIR PERUBAHAN ---
                }
            }
        }
        // --- AKHIR KARTU PROFIL ---

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            CircularProgressIndicator(
                progress = { (xpProgress.toFloat() / maxXp.toFloat()) },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 12.dp,
                color = AccentYellow,
                trackColor = CardBackground
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LEVEL", color = TextColorSecondary, fontSize = 16.sp)
                Text(
                    text = "$level",
                    color = TextColorPrimary,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("$xpProgress/$maxXp XP", color = TextColorSecondary, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(title = "STREAK", value = "$streak HARI", icon = Icons.Default.CheckCircle, iconColor = Color.Red)
            StatCard(title = "TOTAL XP", value = "$totalXp", icon = Icons.Default.Star, iconColor = AccentYellow)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onScheduleReminderClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, PrimaryColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifikasi", tint = PrimaryColor)
                Spacer(Modifier.width(8.dp))
                Text("Aktifkan Pengingat Harian", color = PrimaryColor)
            }
        }
        Text(
            "Anda akan diingatkan setiap hari.",
            color = TextColorSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}