package com.teknos.oncolly.screens.patient

import androidx.annotation.StringRes

// Classe "Segellada" (Sealed): Defineix tots els tipus de blocs que podem fer servir
sealed class ActivityComponent(val key: String) {

    // 1. INPUT NUMÈRIC (Per Km, Minuts, Gots...)
    // 'key': Nom per al JSON (ex: "distance")
    // 'label': Text que surt a la pantalla (ex: "Distància (Km)")
    data class NumberInput(
        val jsonKey: String,
        @StringRes val label: Int
    ) : ActivityComponent(jsonKey)

    // 2. INPUT DE TEXT (Per Notes, Exercicis...)
    data class TextInput(
        val jsonKey: String,
        @StringRes val label: Int,
        val isTextArea: Boolean = false
    ) : ActivityComponent(jsonKey)

    // 3. CRONÒMETRE (Nou!)
    // Guardarà el temps en minuts automàticament
    data class Stopwatch(
        val jsonKey: String
    ) : ActivityComponent(jsonKey)

    // 4. BOOLEAN (Per Sí/No)
    data class BooleanInput(
        val jsonKey: String,
        @StringRes val label: Int
    ) : ActivityComponent(jsonKey)
}