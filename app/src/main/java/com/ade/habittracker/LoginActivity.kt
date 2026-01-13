package com.ade.habittracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ade.habittracker.ui.components.FallingSnowEffect
import com.ade.habittracker.ui.theme.*

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                LoginScreen(onLoginSuccess = {
                    saveLoginStatus()
                    goToMainActivity()
                })
            }
        }
    }

    private fun saveLoginStatus() {
        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // --- CONTAINER UTAMA ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Menggunakan warna gelap manual agar tidak putih
            // Anda bisa ganti Color(0xFF121212) dengan warna background tema Anda jika ada
            .background(Color(0xFF121212))
    ) {

        // 1. Efek Salju (Layer paling bawah)
        FallingSnowEffect(
            modifier = Modifier.fillMaxSize().zIndex(0f)
        )

        // 2. Konten Form (Layer atas)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .zIndex(1f),
            contentAlignment = Alignment.Center
        ) {

            // --- KARTU LOGIN ---
            Card(
                colors = CardDefaults.cardColors(
                    // Menggunakan CardBackground dari tema, sedikit transparan
                    containerColor = CardBackground.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp), // Padding dalam kartu lebih besar
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // JUDUL
                    Text(
                        text = "LOGIN",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentYellow, // Kuning
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = "Lanjutkan misimu hari ini.",
                        fontSize = 14.sp,
                        color = TextColorSecondary, // Abu-abu terang
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                    )

                    // INPUT ID
                    OutlinedTextField(
                        value = userId,
                        onValueChange = { userId = it },
                        label = { Text("ID Petualang") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentYellow,
                            unfocusedBorderColor = TextColorSecondary,
                            focusedLabelColor = AccentYellow,
                            unfocusedLabelColor = TextColorSecondary,
                            cursorColor = AccentYellow,
                            focusedTextColor = TextColorPrimary,
                            unfocusedTextColor = TextColorPrimary
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // INPUT PASSWORD
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Kata Sandi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentYellow,
                            unfocusedBorderColor = TextColorSecondary,
                            focusedLabelColor = AccentYellow,
                            unfocusedLabelColor = TextColorSecondary,
                            cursorColor = AccentYellow,
                            focusedTextColor = TextColorPrimary,
                            unfocusedTextColor = TextColorPrimary
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // TOMBOL LOGIN
                    Button(
                        onClick = onLoginSuccess,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentYellow,
                            contentColor = Color.Black // Teks hitam kontras
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Text(
                            text = "MASUK",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}