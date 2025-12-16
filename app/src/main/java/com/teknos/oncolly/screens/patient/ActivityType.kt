package com.teknos.oncolly.screens.patient

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Wc
import com.teknos.oncolly.R

enum class ActivityType(
    val id: String,
    @StringRes val title: Int,       // Canviat String -> Int
    val icon: ImageVector,
    val color: Color,
    val inputType: InputType,
    @StringRes val labelInput: Int   // Canviat String -> Int
) {
    // 1. WALKING
    WALKING("walking", R.string.walking_patient_screen, Icons.Default.DirectionsWalk, Color(0xFF259DF4), InputType.NUMBER, R.string.label_input_walking),

    // 2. EXERCISE
    EXERCISE("exercise", R.string.exercise_patient_screen, Icons.Default.FitnessCenter, Color(0xFF259DF4), InputType.TEXT, R.string.label_input_exercise),

    // 3. EATING
    EATING("eating", R.string.eating_patient_screen, Icons.Default.Restaurant, Color(0xFF66BB6A), InputType.TEXT_AREA, R.string.label_input_eating),

    // 4. DEPOSITIONS
    MEDITATION("depositions", R.string.depositions_patient_screen, Icons.Default.Wc, Color(0xFF565D6D), InputType.BOOLEAN, R.string.label_input_depositions),

    // 5. MEDICATION
    MEDICATION("medication", R.string.medication_patient_screen, Icons.Default.Medication, Color(0xFF565D6D), InputType.TEXT, R.string.label_input_medication),

    // 6. SLEEP
    SLEEP("sleep", R.string.sleep_patient_screen, Icons.Default.Bed, Color(0xFF66BB6A), InputType.NUMBER, R.string.label_input_sleep),

    // 7. HYDRATION
    HYDRATION("hydration", R.string.hydration_patient_screen, Icons.Default.LocalDrink, Color(0xFF259DF4), InputType.NUMBER, R.string.label_input_hydration);
}

