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
        val patientId = app.userId ?: return // Si no hi ha ID, no fem res

        viewModelScope.launch {
            try {
                isLoading = true
                val response = app.api.getActivities(token, patientId)
                if (response.isSuccessful && response.body() != null) {
                    activities.clear()
                    activities.addAll(response.body()!!)
                } else {
                    errorMessage = "Error carregant: ${response.code()}"
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