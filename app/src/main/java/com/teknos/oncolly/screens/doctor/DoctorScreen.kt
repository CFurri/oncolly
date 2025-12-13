package com.teknos.oncolly.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teknos.oncolly.entity.Pacient // Assegura't que l'import és correcte
import com.teknos.oncolly.singletons.SingletonApp

// Definim les 3 opcions del menú inferior
enum class DoctorTab(val icon: ImageVector, val title: String) {
    PACIENTS(Icons.Default.Home, "Pacients"),
    AGENDA(Icons.Default.DateRange, "Agenda"),
    PERFIL(Icons.Default.Person, "Perfil")
}

@Composable
fun DoctorScreen(
    onLogout: () -> Unit,
    onPacientClick: (String) -> Unit
) {
    // Estat per saber quina pestanya tenim seleccionada (per defecte PACIENTS)
    var selectedTab by remember { mutableStateOf(DoctorTab.PACIENTS) }

    // Estat per guardar la llista que ve del servidor
    var llistaPacients by remember { mutableStateOf<List<Pacient>>(emptyList()) }
    var errorServidor by remember { mutableStateOf<String?>(null) }

    // --- CONNEXIÓ AL SERVIDOR ---
    LaunchedEffect(true) {
        try {
            val api = SingletonApp.getInstance().api
            // Recuperem el token que hem guardat al login
            val token = "Bearer ${SingletonApp.getInstance().userToken}"

            val resposta = api.getPacients(token)

            if (resposta.isSuccessful) {
                llistaPacients = resposta.body() ?: emptyList()
            } else {
                errorServidor = "Error: ${resposta.code()}"
            }
        } catch (e: Exception) {
            errorServidor = "Error de connexió"
            println(e.message)
        }
    }

    // Aquesta és l'estructura base de la pantalla
    Scaffold(
        // 1. BARRA INFERIOR (NAVIGATION UI)
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                DoctorTab.values().forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF6200EE),
                            indicatorColor = Color(0xFFE3F2FD)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        // 2. CONTINGUT PRINCIPAL (Canvia segons la pestanya)
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedTab) {
                DoctorTab.PACIENTS -> PantallaLlistaPacients(llistaPacients,
                    onPacientClick as (String) -> Unit
                )
                DoctorTab.AGENDA -> PantallaPlaceholder("📅 Agenda de Cites")
                DoctorTab.PERFIL -> PantallaPerfilDoctor(onLogout)
            }
        }
    }
}

// --- SUB-PANTALLA 1: LLISTA AMB BUSCADOR ---
@Composable
fun PantallaLlistaPacients(
    totsElsPacients: List<Pacient>,
    onPacientClick: (String) -> Unit
) {
    // Estat del text del buscador
    var textBuscador by remember { mutableStateOf("") }

    // Lògica de filtratge (Busquem per nom)
    val pacientsFiltrats = if (textBuscador.isEmpty()) {
        totsElsPacients
    } else {
        totsElsPacients.filter { it.email.contains(textBuscador, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Text("Els teus Pacients", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6200EE))
        Spacer(modifier = Modifier.height(16.dp))

        // EL BUSCADOR
        OutlinedTextField(
            value = textBuscador,
            onValueChange = { textBuscador = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar per nom...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // LA LLISTA (LazyColumn)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pacientsFiltrats) { pacient ->
                ItemPacientDisseny(pacient, onPacientClick)
            }
        }
    }
}

// Disseny de la targeta individual
@Composable
fun ItemPacientDisseny(pacient: Pacient, onClick: (String) -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().clickable { onClick(pacient.id) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cercle amb la inicial (opcional, queda maco)
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFFE3F2FD)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = pacient.email.first().toString().uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Dades del Pacient
            Column(modifier = Modifier.weight(1f)) {
                // Fem servir l'email com a nom principal
                Text(text = pacient.email, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                // Si tenim telèfon, el mostrem. Si no, text buit.
                val telefon = pacient.phoneNumber ?: "Sense telèfon"
                Text(text = telefon, color = Color.Gray, fontSize = 14.sp)
            }

            // --- AQUI ABANS HI HAVIA LA GRAVETAT ---
            // Com que el servidor no ens la diu, de moment posem un indicador genèric
            // o l'eliminem directament.
            Text(
                text = "Actiu", // Text fix provisional
                color = Color(0xFF2E7D32), // Verd
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

// --- SUB-PANTALLES SIMPLES (Agenda i Perfil) ---

@Composable
fun PantallaPerfilDoctor(onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Perfil del Doctor", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
            Text("Tancar Sessió")
        }
    }
}

@Composable
fun PantallaPlaceholder(titol: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(titol, fontSize = 20.sp, color = Color.Gray)
    }
}