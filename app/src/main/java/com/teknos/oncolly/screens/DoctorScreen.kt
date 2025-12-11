package com.teknos.oncolly.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teknos.oncolly.singletons.SingletonApp

// --- PANTALLA DOCTOR ---
@Composable
fun DoctorScreen(onLogout: () -> Unit) {

    // RECUPEREM DADES
    val role = SingletonApp.getInstance().userRole
    val id = SingletonApp.getInstance().userId

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFE3F2FD)), // Blauet mèdic
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("👨‍⚕️ Panell del Doctor", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Llistat de pacients...", modifier = Modifier.padding(16.dp))

        Button(onClick = onLogout) { Text("Tancar Sessió") }
    }
}