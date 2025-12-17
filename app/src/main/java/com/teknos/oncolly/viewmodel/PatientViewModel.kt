package com.teknos.oncolly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teknos.oncolly.entity.CreateActivityRequest
import com.teknos.oncolly.singletons.ActivitySingleton
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

class PatientViewModel : ViewModel() {

    /**
     * Funció per enviar una activitat al servidor.
     * * @param tipusId: L'identificador de l'activitat (ex: "walking", "medication").
     * @param valor: El valor introduït per l'usuari (ex: "30 min", "Ibuprofè").
     * @param onSuccess: Codi a executar si tot va bé (normalment tancar pantalla).
     * @param onError: Codi a executar si falla (mostrar un missatge).
     */
    fun guardarActivitat(
        tipusId: String,
        valor: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            // 2. Creem l'objecte que espera el servidor
            // El servidor vol: id (UUID), activityType, value, occurredAt
            val request = CreateActivityRequest(
                id = UUID.randomUUID().toString(), // Generem un ID únic per a aquesta activitat
                activityType = tipusId,            // Ex: "walking"
                value = valor,                     // Ex: "2000 passes"
                occurredAt = LocalDateTime.now().toString() // Data i hora actual (ISO 8601)
            )

            // 3. Fem la crida mitjançant el Singleton
            val result = ActivitySingleton.createActivity(request)

            result.onSuccess {
                println("Activitat guardada correctament: $tipusId - $valor")
                onSuccess()
            }.onFailure { e ->
                val errorMsg = "Error: ${e.message}"
                println(errorMsg)
                onError(errorMsg)
            }
        }
    }
}