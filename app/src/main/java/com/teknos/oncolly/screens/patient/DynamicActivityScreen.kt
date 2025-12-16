package com.teknos.oncolly.screens.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teknos.oncolly.R
import com.teknos.oncolly.viewmodel.PatientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicActivityScreen(
    activityType: ActivityType,
    viewModel: PatientViewModel,
    onBack: () -> Unit
) {
    var valorInput by remember { mutableStateOf("") }

    // Obtenim els textos traduïts fent servir l'ID de l'Enum
    val titolActivitat = stringResource(activityType.title)
    val etiquetaInput = stringResource(activityType.labelInput)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.tornar_enrere),
                            tint = activityType.color,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
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
            // Icona
            Surface(shape = CircleShape, color = activityType.color, modifier = Modifier.size(100.dp)) {
                Icon(activityType.icon, contentDescription = null, tint = Color.White, modifier = Modifier.padding(20.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Títol Traduït
            Text(titolActivitat, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = activityType.color)

            Spacer(modifier = Modifier.height(32.dp))

            // Inputs
            when (activityType.inputType) {
                InputType.NUMBER -> {
                    OutlinedTextField(
                        value = valorInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) valorInput = it },
                        label = { Text(etiquetaInput) }, // Label traduïda
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                InputType.TEXT -> {
                    OutlinedTextField(
                        value = valorInput,
                        onValueChange = { valorInput = it },
                        label = { Text(etiquetaInput) }, // Label traduïda
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                InputType.TEXT_AREA -> {
                    OutlinedTextField(
                        value = valorInput,
                        onValueChange = { valorInput = it },
                        label = { Text(etiquetaInput) }, // Label traduïda
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        maxLines = 5
                    )
                }
                InputType.BOOLEAN -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        // Botons traduïts
                        Button(
                            onClick = { valorInput = "Sí" },
                            colors = ButtonDefaults.buttonColors(containerColor = if(valorInput == "Sí") activityType.color else Color.Gray)
                        ) { Text(stringResource(R.string.boto_si)) }

                        Button(
                            onClick = { valorInput = "No" },
                            colors = ButtonDefaults.buttonColors(containerColor = if(valorInput == "No") activityType.color else Color.Gray)
                        ) { Text(stringResource(R.string.boto_no)) }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.guardarActivitat(activityType.id, valorInput, onSuccess = onBack, onError = {})
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = activityType.color),
                enabled = valorInput.isNotEmpty()
            ) {
                Text(stringResource(R.string.guardar_registre_dynamicActivityScreen))
            }
        }
    }
}