package com.teknos.oncolly.screens.doctor

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.List
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
import com.teknos.oncolly.utils.PdfGenerator

//Pel JSON de l'etiqueta
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacientDetailScreen(pacientId: String, onBack: () -> Unit) {

    var pacient by remember { mutableStateOf<Pacient?>(null) }
    var activitatsList by remember { mutableStateOf<List<Activity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val api = SingletonApp.getInstance().api
            val token = "Bearer ${SingletonApp.getInstance().userToken}"

            val respPacients = api.getPacients(token)
            if (respPacients.isSuccessful) {
                pacient = respPacients.body()?.find { it.id == pacientId }
            }

            val respActivitats = api.getActivities(token, pacientId)
            if (respActivitats.isSuccessful) {
                activitatsList = respActivitats.body() ?: emptyList()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = BgLight, // Use consistent BgLight
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if(pacient != null) "${pacient!!.firstName} ${pacient!!.lastName}" else "Carregant...",
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Enrere", tint = PrimaryBlue) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Custom Tabs
            Row(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                TabItem(
                    text = "Activitats",
                    icon = Icons.AutoMirrored.Filled.List,
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                TabItem(
                    text = "Fitxa",
                    icon = Icons.Default.Info,
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBlue)
                } else if (pacient != null) {
                    when (selectedTab) {
                        0 -> LlistaActivitatsPacient(activitatsList)
                        1 -> ContingutPacient(pacient!!)
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.TabItem(text: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) PrimaryBlue else TextGrey
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text, color = color, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        if (isSelected) {
            Box(modifier = Modifier.height(2.dp).width(40.dp).background(PrimaryBlue))
        }
    }
}

@Composable
fun LlistaActivitatsPacient(llista: List<Activity>) {
    if (llista.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aquest pacient encara no ha registrat activitat.", color = TextGrey)
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
    // 1. Determinem icona i color segons el tipus
    val (icon, color) = when (activitat.activityType.lowercase()) {
        "walking" -> Pair(Icons.AutoMirrored.Filled.DirectionsWalk, PrimaryBlue)
        "medication" -> Pair(Icons.Default.Medication, Color(0xFF565D6D))
        "eating" -> Pair(Icons.Default.Restaurant, SecondaryGreen)
        "sleep" -> Pair(Icons.Default.Bed, SecondaryGreen)
        "hydration" -> Pair(Icons.Default.LocalDrink, PrimaryBlue)
        "exercise" -> Pair(Icons.Default.FitnessCenter, PrimaryBlue)
        "depositions" -> Pair(Icons.Default.Wc, Color.Gray)
        else -> Pair(Icons.Default.Info, TextGrey)
    }

    Card(
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona Visual
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))

            // Contingut Central (Títol + Dades Formatades)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activitat.activityType.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextGrey
                )
                // AQUÍ ÉS ON FEM LA MÀGIA DEL JSON:
                SmartActivityContentDoctor(rawContent = activitat.value, color = TextDark)
            }

            // Data Formatada Bonic
            Text(
                text = formatDatePrettyDoctor(activitat.occurredAt),
                fontSize = 12.sp,
                color = TextGrey
            )
        }
    }
}

@Composable
fun ContingutPacient(pacient: Pacient) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = PrimaryBlue.copy(alpha = 0.1f), modifier = Modifier.size(60.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = pacient.firstName.take(1).uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "${pacient.firstName} ${pacient.lastName}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(text = "Pacient Actiu", fontSize = 14.sp, color = SecondaryGreen, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SHARE BUTTON
        Button(
            onClick = {
                val success = PdfGenerator.shareExistingPdf(context, pacient.firstName, pacient.lastName)
                if (!success) {
                    Toast.makeText(context, "No s'ha trobat el PDF original. No es pot regenerar sense contrasenya.", Toast.LENGTH_LONG).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Compartir Credencials", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        InfoCard(
            titol = "Correu Electrònic",
            valor = pacient.email,
            icon = Icons.Default.Email
        )
        Spacer(modifier = Modifier.height(12.dp))
        InfoCard(
            titol = "Telèfon de contacte",
            valor = pacient.phoneNumber ?: "No informat",
            icon = Icons.Default.Phone
        )
        Spacer(modifier = Modifier.height(12.dp))
        InfoCard(
            titol = "Data de Naixement",
            valor = pacient.dateOfBirth ?: "Desconeguda",
            icon = Icons.Default.DateRange
        )
    }
}

@Composable
fun InfoCard(titol: String, valor: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryBlue)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = titol, fontSize = 12.sp, color = TextGrey)
                Text(text = valor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            }
        }
    }
}

// --- FUNCIONS D'AJUDA PER FORMATAR DADES AL DOCTOR ---

@Composable
fun SmartActivityContentDoctor(rawContent: String, color: Color) {
    val dades = remember(rawContent) {
        try {
            val json = JSONObject(rawContent)
            val map = mutableMapOf<String, String>()
            json.keys().forEach { key -> map[key] = json.getString(key) }
            map
        } catch (e: Exception) { null }
    }

    if (dades == null) {
        // Si no és JSON (dades velles), mostra el text tal qual
        Text(text = rawContent, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = color)
    } else {
        // Si és JSON, mostra llista neta (ignorant cronòmetre i camps buits)
        Column {
            dades.forEach { (key, value) ->
                if (key != "time_stopwatch" && value.isNotBlank()) {
                    val unitat = when (key) {
                        "distance" -> "km"
                        "duration" -> "min"
                        "hours" -> "h"
                        "glasses" -> "gots"
                        // Pels camps de text o boolean, no posem unitat
                        "drug_name", "result", "description", "detail" -> ""
                        else -> ""
                    }
                    Text(
                        text = "$value $unitat",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C3E50)
                    )
                }
            }
        }
    }
}

fun formatDatePrettyDoctor(isoDate: String): String {
    return try {
        val parsedDate = LocalDateTime.parse(isoDate)
        // Format curt per llista: 17/12 - 12:30
        val formatter = DateTimeFormatter.ofPattern("dd/MM - HH:mm")
        parsedDate.format(formatter)
    } catch (e: Exception) {
        isoDate.take(10) // Fallback si falla
    }
}