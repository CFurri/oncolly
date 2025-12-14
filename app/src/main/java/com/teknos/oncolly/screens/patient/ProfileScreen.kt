package com.teknos.oncolly.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teknos.oncolly.singletons.SingletonApp


@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToActivities: () -> Unit
) {
    // 1. Recuperem les dades de memòria
    val pacient = SingletonApp.getInstance().pacientActual
    val email = pacient?.email ?: "No disponible"
    val telefon = pacient?.phoneNumber ?: "Sense telèfon"
    val dataNaixement = pacient?.dateOfBirth ?: "No indicada"

    Scaffold(
        bottomBar = {
            // 2. Barra de navegació amb Profile seleccionat (tab 2)
            BottomNavigationBar(
                currentTab = 2,
                onNavigateToHome = onNavigateToHome,
                onNavigateToActivities = onNavigateToActivities,
                onNavigateToProfile = {} // Ja hi som
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- CAPÇALERA ---
            Text("El meu Perfil", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextGrey)
            Spacer(modifier = Modifier.height(32.dp))

            // Icona gran
            Surface(
                shape = CircleShape,
                color = PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        modifier = Modifier.size(50.dp),
                        tint = PrimaryBlue
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // --- DADES DEL PACIENT ---
            ProfileItem(icon = Icons.Default.Email, label = "Email", value = email)
            ProfileItem(icon = Icons.Default.Phone, label = "Telèfon", value = telefon)
            ProfileItem(icon = Icons.Default.Today, label = "Data Naixement", value = dataNaixement)

            Spacer(modifier = Modifier.weight(1f))

            // --- BOTÓ DE LOGOUT ---
            Button(
                onClick = {
                    SingletonApp.getInstance().tancarSessio()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tancar Sessió", color = Color.Red)
            }
        }
    }
}

@Composable
fun ProfileItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, color = TextGrey, fontWeight = FontWeight.Medium)
        }
    }
}