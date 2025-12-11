package com.teknos.oncolly.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teknos.oncolly.singletons.SingletonApp

// --- PANTALLA DE LOGIN ---

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var missatgeError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Oncolly Login", fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email (prova: doc o pacient)") },
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
                // 2. LLANCEM LA COROUTINE (FIL SECUNDARI)
                scope.launch {
                    try {
                        // A. Obtenim l'API des del Singleton del profe
                        val retrofit = SingletonApp.getInstance().retrofit
                        val apiService = retrofit.create(ApiService::class.java)

                        // B. Fem la crida REAL al servidor (això triga uns mil·lisegons)
                        // NOTA: Si el servidor no està en marxa, això saltarà al 'catch'
                        val resposta = apiService.login(LoginRequest(email, password))

                        // C. Si tot va bé, guardem al Singleton
                        SingletonApp.getInstance().ferLogin(
                            id = resposta.id,
                            role = resposta.role,
                            token = resposta.token
                        )

                        // D. Canviem de pantalla
                        missatgeError = false
                        onLoginSuccess(resposta.role)

                    } catch (e: Exception) {
                        // E. SI FALLA (o el servidor està apagat)
                        // De moment, mantenim la "porta del darrere" per poder treballar:
                        if (email == "doc" && password == "1234") {
                            // Guardem dades falses al Singleton per no petar
                            SingletonApp.getInstance().ferLogin(1, "DOCTOR", "token_fals")
                            onLoginSuccess("doctor")
                        } else if (email == "pacient" && password == "1234") {
                            SingletonApp.getInstance().ferLogin(2, "PACIENT", "token_fals")
                            onLoginSuccess("pacient")
                        } else {
                            // Error real
                            missatgeError = true
                            println("Error de connexió: ${e.message}")
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Entrar")
        }

        if (missatgeError) {
            Text("Error de connexió o credencials incorrectes", color = Color.Red, modifier = Modifier.padding(top = 16.dp))
        }
    }
}