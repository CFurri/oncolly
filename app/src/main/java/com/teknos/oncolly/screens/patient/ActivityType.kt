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
        id = "exercise",
        title = R.string.exercise_patient_screen,
        icon = Icons.Default.FitnessCenter,
        color = Color(0xFF259DF4),
        components = listOf(
            // Ara poden cronometrar la sessió de ioga/estiraments
            ActivityComponent.Stopwatch(jsonKey = "time_stopwatch"),
            // O posar el temps manualment
            ActivityComponent.NumberInput(jsonKey = "duration", label = R.string.label_minuts),
            // I explicar què han fet
            ActivityComponent.TextInput(jsonKey = "detail", label = R.string.label_input_exercise, isTextArea = true)
        )
    ),

    // 3. EATING (Text Area)
    EATING(
        id = "eating",
        title = R.string.eating_patient_screen,
        icon = Icons.Default.Restaurant,
        color = Color(0xFF66BB6A),
        components = listOf(
            ActivityComponent.TextInput(jsonKey = "description", label = R.string.label_input_eating, isTextArea = true),
            // Molt útil per al doctor saber si l'àpat ha sentat bé
            ActivityComponent.BooleanInput(jsonKey = "nausea", label = R.string.label_nausea)
        )
    ),

    // 4. DEPOSITIONS (Boolean)
    DEPOSITIONS(
        id = "depositions",
        title = R.string.depositions_patient_screen,
        icon = Icons.Default.Wc,
        color = Color(0xFF565D6D),
        components = listOf(
            ActivityComponent.BooleanInput(jsonKey = "result", label = R.string.label_input_depositions),
            // Camp opcional per si volen anotar color o consistència (important mèdicament)
            ActivityComponent.TextInput(jsonKey = "notes", label = R.string.label_notes)
        )
    ),

    // 5. MEDICATION (Text)
    MEDICATION(
        id = "medication",
        title = R.string.medication_patient_screen,
        icon = Icons.Default.Medication,
        color = Color(0xFF565D6D),
        components = listOf(
            ActivityComponent.TextInput(jsonKey = "drug_name", label = R.string.label_input_medication),
            // Nova casella per la quantitat
            ActivityComponent.TextInput(jsonKey = "dosage", label = R.string.label_dosi)
        )
    ),

    // 6. SLEEP (Numero)
    SLEEP(
        id = "sleep",
        title = R.string.sleep_patient_screen,
        icon = Icons.Default.Bed,
        color = Color(0xFF66BB6A),
        components = listOf(
            ActivityComponent.NumberInput(jsonKey = "hours", label = R.string.label_input_sleep),
            ActivityComponent.BooleanInput(jsonKey = "restful", label = R.string.label_descansat)
        )
    ),

    // 7. HYDRATION (Numero)
    HYDRATION(
        id = "hydration",
        title = R.string.hydration_patient_screen,
        icon = Icons.Default.LocalDrink,
        color = Color(0xFF259DF4),
        components = listOf(
            ActivityComponent.NumberInput(jsonKey = "glasses", label = R.string.label_input_hydration)
        )
    );
}