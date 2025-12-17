package com.teknos.oncolly.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teknos.oncolly.entity.CreatePatientRequest
import com.teknos.oncolly.entity.Pacient
import com.teknos.oncolly.singletons.SingletonApp
import kotlinx.coroutines.launch

import com.teknos.oncolly.entity.UpdatePatientRequest

import com.teknos.oncolly.singletons.PatientSingleton

data class DoctorUiState(
    val isLoading: Boolean = false,
    val patients: List<Pacient> = emptyList(),
    val error: String? = null,
    val feedback: String? = null
)

class DoctorViewModel : ViewModel() {
    var state by mutableStateOf(DoctorUiState())
        private set

    fun updateDoctorProfile(
        firstName: String,
        lastName: String,
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val app = SingletonApp.getInstance()
                val token = "Bearer ${app.userToken}"
                val request = UpdatePatientRequest(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    phoneNumber = "", // Not managed in Doctor UI
                    dateOfBirth = ""  // Not managed in Doctor UI
                )
                val response = app.api.updateDoctorProfile(token, request)

                if (response.isSuccessful) {
                    // Update local singleton
                    app.doctorActual = app.doctorActual?.copy(
                        firstName = firstName,
                        lastName = lastName,
                        email = email
                    )
                    onSuccess()
                } else {
                    onError("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Connection error: ${e.message}")
            }
        }
    }

    fun loadPatients() {
        state = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = PatientSingleton.fetchPatients()
            result.onSuccess { patients ->
                state = state.copy(
                    isLoading = false,
                    patients = patients
                )
            }.onFailure { e ->
                state = state.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    fun createPatient(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phone: String,
        birthDate: String, // yyyy-MM-dd
        onSuccess: () -> Unit
    ) {
        state = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val request = CreatePatientRequest(firstName, lastName, email, password, phone, birthDate)
            val result = PatientSingleton.createPatient(request)
            
            result.onSuccess {
                state = state.copy(feedback = "Patient created successfully")
                loadPatients()
                onSuccess()
            }.onFailure { e ->
                state = state.copy(
                    isLoading = false,
                    error = "Failed: ${e.message}"
                )
            }
        }
    }

    fun deletePatient(id: String) {
        viewModelScope.launch {
            val result = PatientSingleton.deletePatient(id)
            result.onSuccess {
                loadPatients() // Reload list
            }.onFailure { e ->
                state = state.copy(error = "Delete failed: ${e.message}")
            }
        }
    }

    fun clearError() {
        state = state.copy(error = null)
    }

    fun clearFeedback() {
        state = state.copy(feedback = null)
    }
}
