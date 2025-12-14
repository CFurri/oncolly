package com.teknos.oncolly.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.teknos.oncolly.entity.Pacient
import com.teknos.oncolly.network.LoginRequest
import com.teknos.oncolly.singletons.SingletonApp
import kotlinx.coroutines.launch

// Paleta minimalista i professional
private val BackgroundColor = Color(0xFFFAFAFA)
private val PrimaryColor = Color(0xFF4F46E5)
private val TextPrimary = Color(0xFF0F172A)
private val TextSecondary = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val ErrorColor = Color(0xFFDC2626)

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var missatgeError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo simple
            Image(
                painter = painterResource(id = R.drawable.logo_oncolly),
                contentDescription = "Logo Oncolly",
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Títol net
            Text(
                text = "Oncolly",
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Accés a la teva àrea personal",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    missatgeError = null
                },
                label = { Text("Correu electrònic", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = if (email.isNotEmpty()) PrimaryColor else TextSecondary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor = PrimaryColor,
                    unfocusedLabelColor = TextSecondary,
                    cursorColor = PrimaryColor,
                    errorBorderColor = ErrorColor,
                    errorLabelColor = ErrorColor
                ),
                isError = missatgeError != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    missatgeError = null
                },
                label = { Text("Contrasenya", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = if (password.isNotEmpty()) PrimaryColor else TextSecondary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor = PrimaryColor,
                    unfocusedLabelColor = TextSecondary,
                    cursorColor = PrimaryColor,
                    errorBorderColor = ErrorColor,
                    errorLabelColor = ErrorColor
                ),
                isError = missatgeError != null
            )

            // Error message
            if (missatgeError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = missatgeError ?: "",
                    color = ErrorColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botó principal
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        missatgeError = "Omple tots els camps"
                        return@Button
                    }

                    isLoading = true
                    missatgeError = null

                    scope.launch {
                        try {
                            val api = SingletonApp.getInstance().api
                            val response = api.login(LoginRequest(email, password))

                            if (response.isSuccessful) {
                                val body = response.body()
                                if (body != null) {
                                    val app = SingletonApp.getInstance()
                                    app.ferLogin(body.userId, body.role, body.token)

                                    val tokenAmbBearer = "Bearer ${body.token}"

                                    try {
                                        if (body.role.equals("PACIENT", ignoreCase = true)) {
                                            val respPacient = api.getPacientProfile(tokenAmbBearer)
                                            if (respPacient.isSuccessful) {
                                                app.pacientActual = respPacient.body()
                                            }
                                        } else if (body.role.equals("DOCTOR", ignoreCase = true)) {
                                            val respDoctor = api.getDoctorProfile(tokenAmbBearer, body.userId)
                                            if (respDoctor.isSuccessful) {
                                                app.doctorActual = respDoctor.body()
                                            }
                                        }

                                        Toast.makeText(context, "Benvingut/da!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(body.role)

                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error baixant perfil: ${e.message}", Toast.LENGTH_LONG).show()
                                        onLoginSuccess(body.role)
                                    }
                                }
                            } else {
                                missatgeError = "Credencials incorrectes"
                            }
                        } catch (e: Exception) {
                            missatgeError = "Error de connexió"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    disabledContainerColor = PrimaryColor.copy(alpha = 0.5f),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Iniciar sessió",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Footer subtil
            Text(
                text = "Oncolly © 2024",
                fontSize = 13.sp,
                color = TextSecondary.copy(alpha = 0.6f),
                fontWeight = FontWeight.Normal
            )
        }
    }
}