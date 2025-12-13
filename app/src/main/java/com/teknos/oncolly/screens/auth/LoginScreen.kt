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
                                // 2. AVÍS D'ÈXIT
                                Toast.makeText(context, "Benvingut/da ${body.role}!", Toast.LENGTH_LONG).show()

                                SingletonApp.getInstance().ferLogin(
                                    id = body.userId,
                                    role = body.role,
                                    token = body.token
                                )
                                onLoginSuccess(body.role)
                            }
                        } else {
                            // 3. AVÍS D'ERROR DEL SERVIDOR (Ex: 401 Contrasenya malament)
                            if (response.code() == 401) {
                                Toast.makeText(context, "Email o contrasenya incorrectes", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Error del servidor: ${response.code()}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        // 4. AVÍS DE FALLADA DE XARXA (Sense internet o servidor apagat)
                        // Porta del darrere (Backdoor) per proves
                        if (email == "doc" && password == "1234") {
                            Toast.makeText(context, "Mode Prova: Doctor", Toast.LENGTH_SHORT).show()
                            SingletonApp.getInstance().ferLogin("1", "DOCTOR", "fake_token")
                            onLoginSuccess("doctor")
                        } else {
                            Toast.makeText(context, "Error REAL: ${e.message}", Toast.LENGTH_LONG).show()
                            println("ERROR LOGCAT: ${e.stackTraceToString()}")
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