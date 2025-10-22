package com.ade.habittracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ade.habittracker.data.HabitRepository // Import HabitRepository
import com.ade.habittracker.ui.theme.HabitTrackerTheme // Pastikan import Theme benar
// Import Definisi Warna dari MainActivity (atau pindahkan ke file terpisah)
import com.ade.habittracker.DarkBackground
import com.ade.habittracker.TextColorPrimary
import com.ade.habittracker.PrimaryColor

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val repository = HabitRepository()

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun loginUser(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Login gagal. Periksa kembali email dan password.")
            }
        }
    }

    fun registerUser(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                if (result.user != null) {
                    repository.createUserDocument(email)
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Registrasi gagal. Coba email lain atau periksa koneksi.")
            }
        }
    }
}

class AuthActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (authViewModel.isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            HabitTrackerTheme { // Gunakan Theme yang sudah ada
                AuthScreen(viewModel = authViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Tambahkan OptIn jika belum ada
@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    var isLoginScreen by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // --- PERBAIKAN DI SINI (CARA LAIN) ---
    // Definisikan lambda onSuccess dengan tipe eksplisit () -> Unit
    val onSuccess: () -> Unit = {
        isLoading = false
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
        // Panggil finish() pada context yang sudah pasti Activity
        (context as? ComponentActivity)?.finish()
    }
    // -------------------------

    // Lambda onError sudah benar karena Toast.makeText(...).show() mengembalikan Unit
    val onError: (String) -> Unit = { message ->
        isLoading = false
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isLoginScreen) "SELAMAT DATANG" else "BUAT AKUN BARU",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextColorPrimary
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = TextFieldDefaults.colors( // Gunakan .colors()
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    // ... warna lain ...
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = TextFieldDefaults.colors( // Gunakan .colors()
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    // ... warna lain ...
                )
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isLoading = true
                    // Panggil lambda onSuccess dan onError yang sudah didefinisikan di atas
                    if (isLoginScreen) {
                        viewModel.loginUser(email.trim(), password.trim(), onSuccess, onError)
                    } else {
                        viewModel.registerUser(email.trim(), password.trim(), onSuccess, onError)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isLoginScreen) "LOGIN" else "REGISTER")
                }
            }

            TextButton(onClick = { isLoginScreen = !isLoginScreen }) {
                Text(
                    if (isLoginScreen) "Belum punya akun? Register" else "Sudah punya akun? Login",
                    color = PrimaryColor
                )
            }
        }
    }
}

