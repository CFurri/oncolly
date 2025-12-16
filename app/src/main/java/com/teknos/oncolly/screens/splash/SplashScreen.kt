package com.teknos.oncolly.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.teknos.oncolly.R
import com.teknos.oncolly.singletons.SingletonApp
import com.teknos.oncolly.utils.SessionManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigate: (String) -> Unit) {

    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        delay(1500) // Small delay for branding

        // CHECK SESSION
        val session = SessionManager.getSession(context)
        if (session != null) {
            // Restore Session
            val app = SingletonApp.getInstance()
            app.ferLogin(session.userId, session.role, session.token)
            val tokenBearer = "Bearer ${session.token}"

            try {
                // Pre-fetch profile to avoid empty screens
                if (session.role.equals("PATIENT", ignoreCase = true)) {
                    val p = app.api.getPacientProfile(tokenBearer)
                    if (p.isSuccessful) app.pacientActual = p.body()
                    onNavigate("home_pacient")
                } else {
                    val d = app.api.getDoctorProfile(tokenBearer, session.userId)
                    if (d.isSuccessful) app.doctorActual = d.body()
                    onNavigate("home_doctor")
                }
            } catch (e: Exception) {
                // If fetch fails, go to login just in case
                onNavigate("login")
            }
        } else {
            onNavigate("login")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- LOGO ---
        Image(
            painter = painterResource(id = R.drawable.logo_oncolly),
            contentDescription = "Logo Oncolly",
            modifier = Modifier.size(300.dp)
        )
    }
}