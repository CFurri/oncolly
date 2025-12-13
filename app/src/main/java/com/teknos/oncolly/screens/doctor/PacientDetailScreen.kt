package com.teknos.oncolly.screens.doctor

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teknos.oncolly.entity.Pacient
import com.teknos.oncolly.singletons.SingletonApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacientDetailScreen(pacientId: String, onBack: () -> Unit) {

    // Estats per gestionar la càrrega de dades
    var pacient by remember { mutableStateOf<Pacient?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. CRIDA AL SERVIDOR (LaunchedEffect)
    LaunchedEffect(Unit) {
        try {
            val api = SingletonApp.getInstance().api
            val token = "Bearer ${SingletonApp.getInstance().userToken}"

            // Com que no tenim un endpoint "getPacient(id)", demanem la llista i filtrem
            val response = api.getPacients(token)

            if (response.isSuccessful) {
                val llista = response.body() ?: emptyList()
                // Busquem el pacient que coincideix amb l'ID
                pacient = llista.find { it.id == pacientId }
            } else {
                Toast.makeText(context, "Error carregant dades", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fitxa del Pacient") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Enrere")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE3F2FD),
                    titleContentColor = Color(0xFF1976D2)
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                // Roda de càrrega al centre
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (pacient != null) {
                // DADES REALS DEL PACIENT
                ContingutPacient(pacient!!)
            } else {
                // Si no trobem el pacient
                Text(
                    text = "No s'ha trobat el pacient amb ID: $pacientId",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ContingutPacient(pacient: Pacient) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // CAPÇALERA AMB L'EMAIL (Fent de nom)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                // El servidor no té "Nom", fem servir l'Email
                Text(text = pacient.email, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "Pacient Actiu", fontSize = 14.sp, color = Color(0xFF2E7D32))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(24.dp))

        // TARGETA 1: EMAIL
        InfoCard(
            titol = "Correu Electrònic",
            valor = pacient.email,
            icon = Icons.Default.Email,
            colorFons = Color(0xFFE3F2FD) // Blau
        )

        Spacer(modifier = Modifier.height(16.dp))

        // TARGETA 2: TELÈFON (Si n'hi ha)
        InfoCard(
            titol = "Telèfon de contacte",
            valor = pacient.phoneNumber ?: "No informat",
            icon = Icons.Default.Phone,
            colorFons = Color(0xFFE8F5E9) // Verd
        )

        Spacer(modifier = Modifier.height(16.dp))

        // TARGETA 3: DATA NAIXEMENT
        InfoCard(
            titol = "Data de Naixement",
            valor = pacient.dateOfBirth ?: "Desconeguda",
            icon = Icons.Default.DateRange, // Si no tens aquesta icona, posa DateRange
            colorFons = Color(0xFFFFF3E0) // Taronja
        )
    }
}

@Composable
fun InfoCard(titol: String, valor: String, icon: ImageVector?, colorFons: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorFons),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column {
                Text(text = titol, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                Text(text = valor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}