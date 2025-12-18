package com.teknos.oncolly.screens.patient

import com.teknos.oncolly.R
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.*

enum class ActivityType(
    val id: String,
    @StringRes val title: Int,
    val icon: ImageVector,
    val color: Color,
    // Una llista de components en lloc d'un sol InputType
    val components: List<ActivityComponent>
) {


    // 1. WALKING: CRONÒMETRE + MINUTS + KM
    WALKING(
        id = "walking",
        title = R.string.walking_patient_screen,
        icon = Icons.Default.DirectionsWalk,
        color = Color(0xFF259DF4),
        components = listOf(
            // Component 1: El cronòmetre (guarda el temps a la clau "time_stopwatch")
            ActivityComponent.Stopwatch(jsonKey = "time_stopwatch"),

            // Component 2: Camp manual de minuts (si no volen fer servir el crono)
            ActivityComponent.NumberInput(jsonKey = "duration", label = R.string.label_minuts),

            // Component 3: Camp manual de Km
            ActivityComponent.NumberInput(jsonKey = "distance", label = R.string.label_km)
        )
    ),

    // --- ADAPTACIÓ DE LA RESTA D'ACTIVITATS AL NOU SISTEMA ---

    // 2. EXERCISE (Text simple)
    EXERCISE(
        "exercise", R.string.exercise_patient_screen, Icons.Default.FitnessCenter, Color(0xFF259DF4),
        listOf(ActivityComponent.TextInput("detail", R.string.label_input_exercise))
    ),

    // 3. EATING (Text Area)
    EATING(
        "eating", R.string.eating_patient_screen, Icons.Default.Restaurant, Color(0xFF66BB6A),
        listOf(ActivityComponent.TextInput("description", R.string.label_input_eating, isTextArea = true))
    ),

    // 4. DEPOSITIONS (Boolean)
    MEDITATION( // Recorda canviar el nom de l'Enum si pots, sinó ho deixem així
        "depositions", R.string.depositions_patient_screen, Icons.Default.Wc, Color(0xFF565D6D),
        listOf(ActivityComponent.BooleanInput("result", R.string.label_input_depositions))
    ),

    // 5. MEDICATION (Text)
    MEDICATION(
        "medication", R.string.medication_patient_screen, Icons.Default.Medication, Color(0xFF565D6D),
        listOf(ActivityComponent.TextInput("drug_name", R.string.label_input_medication))
    ),

    // 6. SLEEP (Numero)
    SLEEP(
        "sleep", R.string.sleep_patient_screen, Icons.Default.Bed, Color(0xFF66BB6A),
        listOf(ActivityComponent.NumberInput("hours", R.string.label_input_sleep))
    ),

    // 7. HYDRATION (Numero)
    HYDRATION(
        "hydration", R.string.hydration_patient_screen, Icons.Default.LocalDrink, Color(0xFF259DF4),
        listOf(ActivityComponent.NumberInput("glasses", R.string.label_input_hydration))
    );
}