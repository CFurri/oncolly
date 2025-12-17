package com.teknos.oncolly.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teknos.oncolly.entity.Activity
import com.teknos.oncolly.singletons.ActivitySingleton
import kotlinx.coroutines.launch

class ActivitiesViewModel : ViewModel() {
    // Llista observable per a la UI. Si la modifiquem, la pantalla s'actualitza sola.
    val activities = mutableStateListOf<Activity>()

    var isLoading = false
    var errorMessage: String? = null

    // Carregar activitats inicials
    fun loadActivities() {
        viewModelScope.launch {
            isLoading = true
            val result = ActivitySingleton.fetchMyActivities()
            
            result.onSuccess { llistaDelServidor ->
                activities.clear()
                // 2. L'ordenem per data (occurredAt) de MÉS NOVA a MÉS VELLA (Descending)
                val llistaOrdenada = llistaDelServidor.sortedByDescending { it.occurredAt }
                // 3. L'afegim a la llista de la pantalla
                activities.addAll(llistaOrdenada)
            }.onFailure { e ->
                errorMessage = "Error carregant: ${e.message}"
            }
            isLoading = false
        }
    }

    // Esborrar una activitat
    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            val result = ActivitySingleton.deleteActivity(activity.id)
            
            result.onSuccess {
                // 2. Si el servidor diu OK, l'esborrem de la llista local (la UI s'actualitzarà sola)
                activities.remove(activity)
            }.onFailure { e ->
                errorMessage = "No s'ha pogut esborrar: ${e.message}"
            }
        }
    }
}