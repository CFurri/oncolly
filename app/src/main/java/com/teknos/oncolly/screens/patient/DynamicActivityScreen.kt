package com.teknos.oncolly.screens.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Sick
// ... (altres icones que hagis fet servir a l'Enum ActivityType)

// Imports de Material Design 3 (Components visuals)
import androidx.compose.material3.*

// Imports de Runtime (Lògica d'estat)
import androidx.compose.runtime.*

// Imports d'utilitats d'UI (Colors, alineació, tipografia)
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teknos.oncolly.R

// Imports del teu projecte (ViewModel i Tipus)
import com.teknos.oncolly.viewmodel.PatientViewModel
import com.teknos.oncolly.screens.patient.ActivityType
import com.teknos.oncolly.screens.patient.InputType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicActivityScreen(
    activityType: ActivityType,
    viewModel: PatientViewModel,
    onBack: () -> Unit
) {
    // Aquesta variable guardarà el valor, sigui quin sigui (text, numero...)
    var valorInput by remember { mutableStateOf("") }

    Scaffold(
        // AFEGEIX AQUEST BLOC 'topBar'
        topBar = {
            TopAppBar(
                title = { }, // Títol buit perquè ja tens el títol gran a sota
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Tornar",
                            tint = activityType.color, // El botó tindrà el color de l'activitat (blau, verd...)
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent // Fons transparent perquè quedi net
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- CAPÇALERA COMUNA (Sempre igual) ---
            Surface(shape = CircleShape, color = activityType.color, modifier = Modifier.size(100.dp)) {
                Icon(activityType.icon, contentDescription = null, tint = Color.White, modifier = Modifier.padding(20.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(activityType.title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = activityType.color)

            Spacer(modifier = Modifier.height(32.dp))

            // --- EL CERVELL DINÀMIC (Canvia segons el tipus) ---
            when (activityType.inputType) {
                InputType.NUMBER -> {
                    OutlinedTextField(
                        value = valorInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) valorInput = it },
                        label = { Text(activityType.labelInput) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                InputType.TEXT -> {
                    OutlinedTextField(
                        value = valorInput,
                        onValueChange = { valorInput = it },
                        label = { Text(activityType.labelInput) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                InputType.TEXT_AREA -> {
                    OutlinedTextField(
                        value = valorInput,
                        onValueChange = { valorInput = it },
                        label = { Text(activityType.labelInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp), // Més alt
                        maxLines = 5
                    )
                }
                InputType.BOOLEAN -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = { valorInput = "Sí" }, colors = ButtonDefaults.buttonColors(containerColor = if(valorInput == "Sí") activityType.color else Color.Gray)) { Text("SÍ") }
                        Button(onClick = { valorInput = "No" }, colors = ButtonDefaults.buttonColors(containerColor = if(valorInput == "No") activityType.color else Color.Gray)) { Text("NO") }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- BOTÓ GUARDAR COMÚ ---
            Button(
                onClick = {
                    // Enviem el valor tal qual l'hem capturat
                    viewModel.guardarActivitat(activityType.id, valorInput, onSuccess = onBack, onError = {})
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = activityType.color),
                enabled = valorInput.isNotEmpty() // Deshabilitem si està buit
            ) {
                Text(stringResource(R.string.guardar_registre_dynamicActivityScreen))
            }
        }
    }
}