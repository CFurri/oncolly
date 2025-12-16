package com.teknos.oncolly.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel
import com.teknos.oncolly.R
import com.teknos.oncolly.entity.Activity
import com.teknos.oncolly.screens.doctor.PrimaryBlue
import com.teknos.oncolly.screens.doctor.TextGrey
import com.teknos.oncolly.viewmodel.ActivitiesViewModel


@Composable
fun PatientActivitiesScreen(
    viewModel: ActivitiesViewModel = viewModel(),
    // Afegim callbacks de navegació perquè la barra de sota funcioni
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    // Carreguem dades
    LaunchedEffect(Unit) {
        viewModel.loadActivities()
    }

    // Fons Gradient (Igual que a Home)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color.White, PrimaryBlue.copy(alpha = 0.15f))
    )

    Scaffold(
        bottomBar = {
            // Reutilitzem la barra que tenim a PatientScreen.kt
            // (Assegura't que BottomNavigationBar sigui accessible o copia-la aquí també si la tens privata)
            BottomNavigationBar(
                currentTab = 1, // Pestanya del mig activa
                onNavigateToHome = onNavigateToHome,
                onNavigateToActivities = { /* Ja hi som */ },
                onNavigateToProfile = onNavigateToProfile
            )
        },
        containerColor = Color.Transparent // Perquè es vegi el gradient del Box
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundBrush)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Títol gran
                Text(
                    text = stringResource(R.string.historial_d_activitats_patientActivitiesScreen),
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextGrey,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                if (viewModel.activities.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp) // Espai extra al final
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
    }
}

@Composable
fun ActivityItem(activity: Activity, onDeleteClick: () -> Unit) {
    // Recuperem la icona i el color segons el tipus (Walking, Eating...)
    val info = getActivityVisuals(activity.activityType ?: "")

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Icona Visual (Rodona)
            Surface(
                shape = CircleShape,
                color = info.second.copy(alpha = 0.15f), // Fons suau del color de l'activitat
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = info.first,
                        contentDescription = null,
                        tint = info.second, // Icona del color fort
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Textos (Centre)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.activityType?.replaceFirstChar { it.uppercase() } ?: stringResource(
                        R.string.activitat_patientActivitiesScreen
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activity.value ?: "", // Ex: "30 minuts"
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGrey.copy(alpha = 0.8f)
                )

                // Data més petita
                Text(
                    text = activity.occurredAt ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // 3. Botó Esborrar (Discret)
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.esborrar_patientActivitiesScreen),
                    tint = Color(0xFFFF6B6B) // Un vermell suau
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Assignment,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.encara_no_tens_activitats_patientActivitiesScreen), color = Color.Gray)
    }
}

// --- HELPER PER MAPEJAR TEXT -> ICONA/COLOR ---
// Això fa que si l'activitat és "walking", surti la icona de caminar i color blau
fun getActivityVisuals(type: String): Pair<ImageVector, Color> {
    return when (type.lowercase()) {
        "walking" -> Pair(Icons.Default.DirectionsWalk, PrimaryBlue)
        "exercise" -> Pair(Icons.Default.FitnessCenter, PrimaryBlue)
        "eating" -> Pair(Icons.Default.Restaurant, SecondaryGreen)
        "sleep" -> Pair(Icons.Default.Bed, SecondaryGreen)
        "hydration" -> Pair(Icons.Default.LocalDrink, PrimaryBlue)
        "medication" -> Pair(Icons.Default.Medication, PrimaryBlue)
        "depositions" -> Pair(Icons.Default.Wc, Color.Gray)
        else -> Pair(Icons.Default.Assignment, PrimaryBlue) // Per defecte
    }
}