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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Wc

enum class ActivityType(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val inputType: InputType,
    val labelInput: String // <--- Per personalitzar el text del camp
) {
    // 1. WALKING (Minuts o Passos) -> Numèric
    WALKING("walking", "Caminar", Icons.Default.DirectionsWalk, Color(0xFF259DF4), InputType.NUMBER, "Minuts caminant"),

    // 2. EXERCISE (Tipus d'exercici) -> Text
    EXERCISE("exercise", "Exercici", Icons.Default.FitnessCenter, Color(0xFF259DF4), InputType.TEXT, "Quin exercici has fet?"),

    // 3. EATING (Què has menjat?) -> Text llarg
    EATING("eating", "Alimentació", Icons.Default.Restaurant, Color(0xFF66BB6A), InputType.TEXT_AREA, "Descriu el teu àpat"),

    // 4. DEPOSITIONS (Bé?) -> Boolean
    MEDITATION("depositions", "Deposicions", Icons.Default.Wc, Color(0xFF565D6D), InputType.BOOLEAN, "Ha anat bé?"),

    // 5. MEDICATION (Nom medicament) -> Text
    MEDICATION("medication", "Medicació", Icons.Default.Medication, Color(0xFF565D6D), InputType.TEXT, "Medicament pres"),

    // 6. SLEEP (Hores) -> Numèric
    SLEEP("sleep", "Descans", Icons.Default.Bed, Color(0xFF66BB6A), InputType.NUMBER, "Hores dormides"),

    // 7. HYDRATION (Litres o gots) -> Numèric
    HYDRATION("hydration", "Hidratació", Icons.Default.LocalDrink, Color(0xFF259DF4), InputType.NUMBER, "Gots d'aigua");
}

