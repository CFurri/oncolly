package com.teknos.oncolly.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teknos.oncolly.entity.Activity
import com.teknos.oncolly.singletons.SingletonApp
import kotlinx.coroutines.launch

class ActivitiesViewModel : ViewModel() {
    // Llista observable per a la UI. Si la modifiquem, la pantalla s'actualitza sola.
    val activities = mutableStateListOf<Activity>()

    var isLoading = false
    var errorMessage: String? = null

    // Carregar activitats inicials
    fun loadActivities() {
        val app = SingletonApp.getInstance()
        val token = "Bearer ${app.userToken}"

        viewModelScope.launch {
            try {
                isLoading = true
                val response = app.api.getMyActivities(token)
                if (response.isSuccessful && response.body() != null) {
                    activities.clear()

                    // 1. Agafem la llista que ve del servidor
                    val llistaDelServidor = response.body()!!

                    // 2. L'ordenem per data (occurredAt) de MÉS NOVA a MÉS VELLA (Descending)
                    val llistaOrdenada = llistaDelServidor.sortedByDescending { it.occurredAt }

                    // 3. L'afegim a la llista de la pantalla
                    activities.addAll(llistaOrdenada)
                } else {
                    errorMessage = "Error carregant: ${response.code()}"
                    println("Error body: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                errorMessage = "Error de connexió: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Esborrar una activitat
    fun deleteActivity(activity: Activity) {
        val app = SingletonApp.getInstance()
        val token = "Bearer ${app.userToken}"

        viewModelScope.launch {
            try {
                // 1. Esborrem del servidor
                val response = app.api.deleteActivity(token, activity.id)

                if (response.isSuccessful) {
                    // 2. Si el servidor diu OK, l'esborrem de la llista local (la UI s'actualitzarà sola)
                    activities.remove(activity)
                } else {
                    errorMessage = "No s'ha pogut esborrar"
                }
            } catch (e: Exception) {
                errorMessage = "Error intentant esborrar"
            }
        }
    }
}