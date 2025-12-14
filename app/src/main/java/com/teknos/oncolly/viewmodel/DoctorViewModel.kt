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

data class DoctorUiState(
    val isLoading: Boolean = false,
    val patients: List<Pacient> = emptyList(),
    val error: String? = null,
    val feedback: String? = null
)

class DoctorViewModel : ViewModel() {
    var state by mutableStateOf(DoctorUiState())
        private set

    fun loadPatients() {
        state = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val app = SingletonApp.getInstance()
                val token = "Bearer ${app.userToken}"
                val response = app.api.getPacients(token)

                if (response.isSuccessful) {
                    state = state.copy(
                        isLoading = false,
                        patients = response.body() ?: emptyList()
                    )
                } else {
                    state = state.copy(
                        isLoading = false,
                        error = "Error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = "Connection error: ${e.message}"
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
            try {
                val app = SingletonApp.getInstance()
                val token = "Bearer ${app.userToken}"
                
                println("Debug: Create Patient")
                println("User Role: ${app.userRole}")
                println("Token (first 10): ${app.userToken?.take(10)}...")
                println("Request: Name=$firstName $lastName, Email=$email, Phone=$phone, DOB=$birthDate")

                val request = CreatePatientRequest(firstName, lastName, email, password, phone, birthDate)
                val response = app.api.createPatient(token, request)

                if (response.isSuccessful) {
                    println("Success: ${response.code()}")
                    state = state.copy(feedback = "Patient created successfully")
                    loadPatients()
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = if (!errorBody.isNullOrBlank()) errorBody else response.message()
                    println("Failed: Code=${response.code()}, Msg=$errorMsg")

                    val uiError = if (response.code() == 403) {
                        "403 Forbidden: Server denied access. Verify you are logged in as a Doctor and the server has this feature deployed."
                    } else {
                        "Failed (${response.code()}): $errorMsg"
                    }

                    state = state.copy(
                        isLoading = false,
                        error = uiError
                    )
                }
            } catch (e: Exception) {
                println("Exception: ${e.message}")
                e.printStackTrace()
                state = state.copy(
                    isLoading = false,
                    error = "Connection Error: ${e.message}"
                )
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
