package com.teknos.oncolly.screens.doctor

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.teknos.oncolly.entity.Activity
import com.teknos.oncolly.entity.Pacient
import com.teknos.oncolly.singletons.SingletonApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacientDetailScreen(pacientId: String, onBack: () -> Unit) {

    // Estats
    var pacient by remember { mutableStateOf<Pacient?>(null) }
    var activitatsList by remember { mutableStateOf<List<Activity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Control de Pestanyes (0 = Activitats, 1 = Fitxa)
    var selectedTab by remember { mutableIntStateOf(0) }

    val context = LocalContext.current

    // 1. CARREGAR DADES (PACIENT I ACTIVITATS)
    LaunchedEffect(Unit) {
        try {
            val api = SingletonApp.getInstance().api
            val token = "Bearer ${SingletonApp.getInstance().userToken}"

            // A. Carreguem Pacients
            val respPacients = api.getPacients(token)
            if (respPacients.isSuccessful) {
                pacient = respPacients.body()?.find { it.id == pacientId }
            }

            // B. Carreguem Activitats (Si el pacient existeix)
            // NOTA: Si el servidor retorna TOTES les activitats de tothom,
            // hauràs de filtrar aquí per 'it.patientId == pacientId'
            val respActivitats = api.getActivities(token, pacientId)
            if (respActivitats.isSuccessful) {
                // Ja no cal filtrar res, el servidor ens dona només les d'aquest pacient
                activitatsList = respActivitats.body() ?: emptyList()
            } else {
                // Opcional: Pots controlar errors aquí si vols
                println("Error baixant activitats: ${respActivitats.code()}")
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
                title = { Text(if(pacient != null) pacient!!.email else "Carregant...") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Enrere") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE3F2FD))
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // 2. BARRA DE PESTANYES
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Activitats") },
                    icon = { Icon(Icons.Default.List, null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Fitxa Tècnica") },
                    icon = { Icon(Icons.Default.Info, null) }
                )
            }

            // 3. CONTINGUT SEGONS LA PESTANYA
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (pacient != null) {
                    when (selectedTab) {
                        0 -> LlistaActivitatsPacient(activitatsList)
                        1 -> ContingutPacient(pacient!!) // La teva funció antiga
                    }
                }
            }
        }
    }
}

// --- PANTALLA DE LA LLISTA D'ACTIVITATS ---
@Composable
fun LlistaActivitatsPacient(llista: List<Activity>) {
    if (llista.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aquest pacient encara no ha registrat activitat.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(llista) { activitat ->
                ItemActivitat(activitat)
            }
        }
    }
}

@Composable
fun ItemActivitat(activitat: Activity) {
    // Determinem icona i color segons el tipus (fent servir strings del servidor)
    val (icon, color) = when (activitat.activityType.lowercase()) {
        "walking" -> Pair(Icons.Default.DirectionsWalk, Color(0xFF259DF4))
        "medication" -> Pair(Icons.Default.Medication, Color(0xFF565D6D))
        "eating" -> Pair(Icons.Default.Restaurant, Color(0xFF66BB6A))
        "sleep" -> Pair(Icons.Default.Bed, Color(0xFF66BB6A))
        else -> Pair(Icons.Default.Info, Color.Gray)
    }

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona rodona
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activitat.activityType.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = activitat.value,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Data (Si vols formatar-la millor, caldria un parsejador de dates)
            Text(
                text = activitat.occurredAt.take(10), // Només la data "2025-12-13"
                fontSize = 12.sp,
                color = Color.Gray
            )
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
        // CAPÇALERA
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = pacient.email, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "Pacient Actiu", fontSize = 14.sp, color = Color(0xFF2E7D32))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(24.dp))

        // TARGETES D'INFORMACIÓ
        InfoCard(
            titol = "Correu Electrònic",
            valor = pacient.email,
            icon = Icons.Default.Email,
            colorFons = Color(0xFFE3F2FD)
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoCard(
            titol = "Telèfon de contacte",
            valor = pacient.phoneNumber ?: "No informat",
            icon = Icons.Default.Phone,
            colorFons = Color(0xFFE8F5E9)
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoCard(
            titol = "Data de Naixement",
            valor = pacient.dateOfBirth ?: "Desconeguda",
            icon = Icons.Default.DateRange,
            colorFons = Color(0xFFFFF3E0)
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