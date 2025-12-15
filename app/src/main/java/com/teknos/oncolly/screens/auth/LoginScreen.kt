package com.teknos.oncolly.screens.auth

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teknos.oncolly.R
import com.teknos.oncolly.network.LoginRequest
import com.teknos.oncolly.singletons.SingletonApp
import com.teknos.oncolly.utils.SessionManager
import kotlinx.coroutines.launch

// Paleta minimalista i professional
private val BackgroundColor = Color(0xFFF8F9FA)
private val PrimaryBlue = Color(0xFF259DF4)
private val SecondaryGreen = Color(0xFF66BB6A)
private val TextPrimary = Color(0xFF2C3E50)
private val TextSecondary = Color(0xFF7F8C8D)
private val BorderColor = Color(0xFFE2E8F0)
private val ErrorColor = Color(0xFFFF5252)

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var missatgeError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // FUNCTION TO PERFORM LOGIN
    fun performLogin(u: String, p: String) {
        if (u.isBlank() || p.isBlank()) {
            missatgeError = context.getString(R.string.omple_tots_els_camps)
            return
        }
        isLoading = true
        missatgeError = null

        scope.launch {
            try {
                val api = SingletonApp.getInstance().api
                val response = api.login(LoginRequest(u, p))

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val app = SingletonApp.getInstance()
                        app.ferLogin(body.userId, body.role, body.token)
                        
                        // SAVE SESSION
                        SessionManager.saveSession(context, body.token, body.userId, body.role)

                        val tokenAmbBearer = "Bearer ${body.token}"

                        try {
                            if (body.role.equals("PACIENT", ignoreCase = true)) {
                                val respPacient = api.getPacientProfile(tokenAmbBearer)
                                if (respPacient.isSuccessful) {
                                    app.pacientActual = respPacient.body()
                                }
                            } else {
                                val respDoctor = api.getDoctorProfile(tokenAmbBearer, body.userId)
                                if (respDoctor.isSuccessful) {
                                    app.doctorActual = respDoctor.body()
                                }
                            }
                            onLoginSuccess(body.role)

                        } catch (e: Exception) {
                            onLoginSuccess(body.role)
                        }
                    }
                } else {
                    missatgeError = context.getString(R.string.credencials_incorrectes_login)
                }
            } catch (e: Exception) {
                missatgeError = context.getString(R.string.error_de_connexio_login, e.message)
            } finally {
                isLoading = false
            }
        }
    }

    // DEEP LINK CHECK
    LaunchedEffect(Unit) {
        val intent = (context as? Activity)?.intent
        val data = intent?.data
        if (data != null && data.scheme == "oncolly" && data.host == "login") {
            val e = data.getQueryParameter("e")
            val p = data.getQueryParameter("p")
            if (!e.isNullOrBlank() && !p.isNullOrBlank()) {
                email = e
                password = p
                performLogin(e, p)
                intent.data = null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // Gradient Header Decoration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(PrimaryBlue.copy(alpha = 0.1f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_oncolly),
                        contentDescription = "Logo Oncolly",
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.benvingut_a_oncolly),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = stringResource(R.string.la_teva_salut_la_nostra_prioritat_login),
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; missatgeError = null },
                label = { Text(stringResource(R.string.correu_electronic)) },
                leadingIcon = { Icon(Icons.Outlined.Email, null, tint = PrimaryBlue) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    focusedLabelColor = PrimaryBlue,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                isError = missatgeError != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; missatgeError = null },
                label = { Text(stringResource(R.string.contrasenya_login)) },
                leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = PrimaryBlue) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    focusedLabelColor = PrimaryBlue,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                isError = missatgeError != null
            )

            if (missatgeError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = missatgeError ?: "",
                    color = ErrorColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { performLogin(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f),
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = stringResource(R.string.iniciar_sessio_login_button), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
