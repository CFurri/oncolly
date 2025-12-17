package com.teknos.oncolly.screens.patient

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teknos.oncolly.R
import com.teknos.oncolly.viewmodel.PatientViewModel
import kotlinx.coroutines.delay
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicActivityScreen(
    activityType: ActivityType,
    viewModel: PatientViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // -------------------------------------------------------------------------
    // 1. ESTAT GLOBAL DE LA PANTALLA (El "Sac" on guardem totes les dades)
    // -------------------------------------------------------------------------
    // Aquest mapa guardarà: "distance" -> "10", "duration" -> "30", etc.
    val inputValues = remember { mutableStateMapOf<String, Any>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.tornar_enrere),
                            tint = activityType.color,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        // Fons degradat suau
        val backgroundBrush = Brush.verticalGradient(
            colors = listOf(Color.White, activityType.color.copy(alpha = 0.1f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()), // Permet fer scroll si hi ha molts camps
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // -------------------------------------------------------------------------
            // 2. CAPÇALERA VISUAL (Icona i Títol)
            // -------------------------------------------------------------------------
            Surface(
                shape = CircleShape,
                color = activityType.color,
                modifier = Modifier.size(100.dp).padding(4.dp)
            ) {
                Icon(
                    imageVector = activityType.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(activityType.title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = activityType.color
            )

            Spacer(modifier = Modifier.height(32.dp))

            // -------------------------------------------------------------------------
            // 3. RENDERITZADOR DE COMPONENTS (El "Bucle Màgic")
            // -------------------------------------------------------------------------
            // Aquí és on la pantalla decideix què pintar segons la llista d'ingredients

            activityType.components.forEach { component ->
                when (component) {
                    is ActivityComponent.Stopwatch -> {
                        RenderStopwatch(component, activityType.color) { tempsFormatat ->
                            // Quan el crono canvia, actualitzem el mapa global
                            inputValues[component.key] = tempsFormatat
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    is ActivityComponent.NumberInput -> {
                        RenderNumberInput(component, activityType.color, inputValues)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    is ActivityComponent.TextInput -> {
                        RenderTextInput(component, activityType.color, inputValues)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    is ActivityComponent.BooleanInput -> {
                        RenderBooleanInput(component, activityType.color, inputValues)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // -------------------------------------------------------------------------
            // 4. BOTÓ GUARDAR (Empaquetar i Enviar)
            // -------------------------------------------------------------------------
            Button(
                onClick = {
                    // Convertim el mapa de dades a un JSON String (el "paquet")
                    // Exemple resultat: {"duration":"30", "distance":"5"}
                    val jsonDades = JSONObject(inputValues.toMap()).toString()

                    // Enviem aquest paquet com si fos un text normal
                    viewModel.guardarActivitat(
                        activityType.id,
                        jsonDades,
                        onSuccess = {
                            Toast.makeText(context, "Activitat Guardada!", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        onError = {
                            Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = activityType.color),
                // Validació simple: Si el mapa està buit, no deixem guardar (opcional)
                enabled = inputValues.isNotEmpty()
            ) {
                Text(
                    text = stringResource(R.string.guardar_registre_dynamicActivityScreen),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// =============================================================================
// SUB-COMPONENTS (Les "Peces de LEGO")
// =============================================================================

@Composable
fun RenderStopwatch(
    component: ActivityComponent.Stopwatch,
    color: Color,
    onValueChange: (String) -> Unit
) {
    // Estat local del cronòmetre
    var isRunning by remember { mutableStateOf(false) }
    var timeSeconds by remember { mutableStateOf(0L) }

    // El motor del temps (s'activa quan isRunning és true)
    LaunchedEffect(isRunning) {
        val startTime = System.currentTimeMillis() - (timeSeconds * 1000)
        while (isRunning) {
            delay(100) // Actualitzem cada dècima de segon
            timeSeconds = (System.currentTimeMillis() - startTime) / 1000
            // Convertim a format "HH:MM:SS" per guardar-ho
            val h = timeSeconds / 3600
            val m = (timeSeconds % 3600) / 60
            val s = timeSeconds % 60
            onValueChange(String.format("%02d:%02d:%02d", h, m, s))
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visualització del temps (00:00:00)
            val hours = timeSeconds / 3600
            val minutes = (timeSeconds % 3600) / 60
            val seconds = timeSeconds % 60

            Text(
                text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Botons de control (Play/Pause/Reset)
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Botó Reset
                IconButton(onClick = { isRunning = false; timeSeconds = 0; onValueChange("00:00:00") }) {
                    Icon(Icons.Default.Refresh, "Reset", tint = Color.Gray, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(32.dp))

                // Botó Play/Pause Principal
                Button(
                    onClick = { isRunning = !isRunning },
                    colors = ButtonDefaults.buttonColors(containerColor = color),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RenderNumberInput(
    component: ActivityComponent.NumberInput,
    color: Color,
    valuesMap: MutableMap<String, Any>
) {
    var textValue by remember { mutableStateOf("") }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            // Filtre: Només acceptem números
            if (newValue.all { it.isDigit() }) {
                textValue = newValue
                valuesMap[component.key] = newValue
            }
        },
        label = { Text(stringResource(component.label)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = color,
            focusedLabelColor = color
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun RenderTextInput(
    component: ActivityComponent.TextInput,
    color: Color,
    valuesMap: MutableMap<String, Any>
) {
    var textValue by remember { mutableStateOf("") }

    OutlinedTextField(
        value = textValue,
        onValueChange = {
            textValue = it
            valuesMap[component.key] = it
        },
        label = { Text(stringResource(component.label)) },
        modifier = Modifier
            .fillMaxWidth()
            .height(if (component.isTextArea) 120.dp else 60.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = color,
            focusedLabelColor = color
        ),
        shape = RoundedCornerShape(12.dp),
        maxLines = if (component.isTextArea) 5 else 1
    )
}

@Composable
fun RenderBooleanInput(
    component: ActivityComponent.BooleanInput,
    color: Color,
    valuesMap: MutableMap<String, Any>
) {
    // Estat local per saber quin botó està marcat
    var selectedOption by remember { mutableStateOf<String?>(null) }

    Column {
        Text(
            text = stringResource(component.label),
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("SÍ" to R.string.boto_si, "NO" to R.string.boto_no).forEach { (valor, stringId) ->
                val isSelected = selectedOption == valor

                Button(
                    onClick = {
                        selectedOption = valor
                        valuesMap[component.key] = valor
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) color else Color.LightGray
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    Text(stringResource(stringId))
                }
            }
        }
    }
}