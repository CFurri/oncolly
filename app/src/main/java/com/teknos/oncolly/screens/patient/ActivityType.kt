package com.teknos.oncolly.screens.patient

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sick

enum class ActivityType(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val inputType: InputType // <--- Afegeix això per saber què demanar
) {
    WALKING("walking", "Caminar", Icons.Default.DirectionsWalk, Color(0xFF259DF4), InputType.NUMBER),
    MEDICATION("medication", "Medicació", Icons.Default.Medication, Color(0xFF66BB6A), InputType.TEXT),
    SLEEP("sleep", "Dormir", Icons.Default.Bed, Color(0xFF66BB6A), InputType.NUMBER),
    SYMPTOMS("symptoms", "Símptomes", Icons.Default.Sick, Color(0xFFEF5350), InputType.TEXT_AREA)
    // ... afegeix les que vulguis
}

