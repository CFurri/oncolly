package com.teknos.oncolly.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teknos.oncolly.R
import com.teknos.oncolly.network.LoginRequest
import com.teknos.oncolly.singletons.SingletonApp
import kotlinx.coroutines.launch


// --- PANTALLA DE LOGIN ---

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var missatgeError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }


    val scope = rememberCoroutineScope()

    //Context per fer el Toast de /auth
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo_oncolly),
            contentDescription = "Logo Oncolly",
            modifier = Modifier.size(150.dp).padding(bottom = 16.dp)
        )

        Text("Oncolly Login", fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Contrasenya") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    // AVÍS: Camps buits
                    Toast.makeText(context, "Omple tots els camps!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true // Activem càrrega

                scope.launch {
                    try {
                        val api = SingletonApp.getInstance().api
                        val response = api.login(LoginRequest(email, password))

                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null) {
                                // 1. Guardem les credencials bàsiques
                                val app = SingletonApp.getInstance()
                                app.ferLogin(body.userId, body.role, body.token)

                                // 2. BAIXEM EL PERFIL SENCER SEGONS EL ROL
                                val tokenAmbBearer = "Bearer ${body.token}"

                                try {
                                    if (body.role.equals("PACIENT", ignoreCase = true)) {
                                        // Demanem el pacient al servidor
                                        val respPacient = api.getPacientProfile(tokenAmbBearer, body.userId)
                                        if (respPacient.isSuccessful) {
                                            // --- AQUÍ EL GUARDEM A LA MEMÒRIA GLOBAL ---
                                            app.pacientActual = respPacient.body()
                                        }
                                    } else if (body.role.equals("DOCTOR", ignoreCase = true)) {
                                        val respDoctor = api.getDoctorProfile(tokenAmbBearer, body.userId)
                                        if (respDoctor.isSuccessful) {
                                            app.doctorActual = respDoctor.body()
                                        }
                                    }

                                    // 3. Tot llest, naveguem i avisem
                                    Toast.makeText(context, "Benvingut/da ${body.role}!", Toast.LENGTH_LONG).show()
                                    onLoginSuccess(body.role)

                                } catch (e: Exception) {
                                    Toast.makeText(context, "Login correcte però error baixant perfil: ${e.message}", Toast.LENGTH_LONG).show()
                                    // Encara que falli baixar el perfil, potser vols deixar entrar l'usuari igualment:
                                    onLoginSuccess(body.role)
                                }
                            }
                        }
                    } finally {
                         isLoading = false // Desactivem càrrega passi el que passi
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading // Deshabilitem el botó si està carregant
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Entrar")
            }
        }
    }
}