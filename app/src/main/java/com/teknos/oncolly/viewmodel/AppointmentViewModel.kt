package com.teknos.oncolly.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teknos.oncolly.entity.Appointment
import com.teknos.oncolly.entity.CreateAppointmentRequest
import com.teknos.oncolly.repository.AppointmentRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.coroutines.launch

data class AppointmentUiState(
    val isLoading: Boolean = false,
    val appointments: List<Appointment> = emptyList(),
    val error: String? = null,
    val feedback: String? = null
)

class AppointmentViewModel : ViewModel() {

    var state by mutableStateOf(AppointmentUiState())
        private set

    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun loadAppointments() {
        val cached = AppointmentRepository.cached()
        if (cached.isNotEmpty()) {
            state = state.copy(appointments = cached, isLoading = false, error = null)
        } else {
            state = state.copy(isLoading = true, error = null, feedback = null)
        }
        viewModelScope.launch {
            val result = AppointmentRepository.fetchAppointments()
            state = result.fold(
                onSuccess = { list -> state.copy(isLoading = false, appointments = list, feedback = null) },
                onFailure = { err -> state.copy(isLoading = false, error = err.message, feedback = null) }
            )
        }
    }

    fun createAppointment(
        patientId: String,
        start: LocalDateTime,
        end: LocalDateTime,
        title: String,
        notes: String?
    ) {
        val provisional = Appointment(
            id = UUID.randomUUID().toString(),
            doctorId = null,
            doctorName = null,
            patientId = patientId,
            patientName = null,
            startTime = formatter.format(start),
            endTime = formatter.format(end),
            status = "syncing",
            title = title
        )
        AppointmentRepository.addLocal(provisional)
        val optimisticList = (listOf(provisional) + state.appointments).sortedBy { it.startTime }
        state = state.copy(appointments = optimisticList, error = null, feedback = "Saving...")

        val request = CreateAppointmentRequest(
            id = provisional.id,
            patientId = patientId,
            startTime = formatter.format(start),
            endTime = formatter.format(end),
            title = title,
            doctorNotes = notes
        )
        viewModelScope.launch {
            val result = AppointmentRepository.createAppointment(request)
            state = result.fold(
                onSuccess = {
                    val replaced = state.appointments.map {
                        if (it.id == provisional.id) it.copy(status = "scheduled") else it
                    }.sortedBy { it.startTime }
                    AppointmentRepository.addLocal(
                        provisional.copy(status = "scheduled")
                    )
                    state.copy(
                        isLoading = false,
                        appointments = replaced,
                        feedback = "Appointment created"
                    )
                },
                onFailure = { err ->
                    AppointmentRepository.removeLocal(provisional.id)
                    state.copy(
                        isLoading = false,
                        appointments = state.appointments.filterNot { it.id == provisional.id },
                        error = err.message,
                        feedback = null
                    )
                }
            )
        }
    }

    fun deleteAppointment(id: String) {
        val previous = state.appointments
        AppointmentRepository.removeLocal(id)
        state = state.copy(
            appointments = previous.filterNot { it.id == id },
            error = null,
            feedback = "Removing..."
        )
        viewModelScope.launch {
            val result = AppointmentRepository.deleteAppointment(id)
            state = result.fold(
                onSuccess = {
                    state.copy(
                        isLoading = false,
                        feedback = "Appointment removed"
                    )
                },
                onFailure = { err ->
                    previous.forEach { AppointmentRepository.addLocal(it) }
                    state.copy(
                        isLoading = false,
                        error = err.message,
                        appointments = previous,
                        feedback = null
                    )
                }
            )
        }
    }

    fun clearFeedback() {
        state = state.copy(feedback = null)
    }

    fun clearError() {
        state = state.copy(error = null)
    }
}
