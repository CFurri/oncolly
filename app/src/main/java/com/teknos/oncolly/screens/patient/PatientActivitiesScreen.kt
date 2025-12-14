package com.teknos.oncolly.screens.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teknos.oncolly.entity.Activity
import com.teknos.oncolly.viewmodel.ActivitiesViewModel

@Composable
fun PatientActivitiesScreen(
    viewModel: ActivitiesViewModel = viewModel() // Injectem el ViewModel
) {
    // Carreguem les dades només entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadActivities()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Les meves activitats", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.activities.isEmpty()) {
            Text("No tens cap activitat registrada.", color = Color.Gray)
        } else {
            // AQUEST ÉS EL TEU RECYCLERVIEW EN COMPOSE
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.activities) { activitat ->
                    ActivityItem(
                        activity = activitat,
                        onDeleteClick = { viewModel.deleteActivity(activitat) }
                    )
                }
            }
        }
    }
}

// Disseny de cada fila (Card)
@Composable
fun ActivityItem(activity: Activity, onDeleteClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White) // O el color que vulguis
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Etiqueta tipus 'Chip' per al tipus d'activitat
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = activity.activityType ?: "General", // Ex: "Esport", "Dieta"
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Descripció o valor
                Text(
                    text = activity.value ?: "Sense descripció", // Ajusta segons el teu model Activity
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = activity.occurredAt ?: "", // Data si en tens
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Botó d'esborrar
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Esborrar", tint = Color.Red)
            }
        }
    }
}