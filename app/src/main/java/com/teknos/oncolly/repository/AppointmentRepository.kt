package com.teknos.oncolly.repository

import com.teknos.oncolly.entity.Appointment
import com.teknos.oncolly.entity.CreateAppointmentRequest
import com.teknos.oncolly.network.ApiService
import com.teknos.oncolly.singletons.SingletonApp

/**
 * Singleton repository to centralize appointment calls and keep a small in-memory cache.
 */
object AppointmentRepository {
    private val api: ApiService = SingletonApp.getInstance().api
    private val cache: MutableList<Appointment> = mutableListOf()

    private fun bearer(): String =
        SingletonApp.getInstance().userToken?.let { "Bearer $it" }
            ?: error("Missing auth token")

    suspend fun fetchAppointments(): Result<List<Appointment>> = runCatching {
        val response = api.getAppointments(bearer())
        if (response.isSuccessful) {
            val body = response.body().orEmpty()
            cache.clear()
            cache.addAll(body)
            body
        } else {
            error("${response.code()}")
        }
    }

    fun cached(): List<Appointment> = cache.toList()

    suspend fun createAppointment(request: CreateAppointmentRequest): Result<Unit> = runCatching {
        val response = api.createAppointment(bearer(), request)
        if (response.isSuccessful) {
            Unit
        } else {
            val errorBody = response.errorBody()?.string()
            error(errorBody?.ifBlank { "HTTP ${response.code()}" } ?: "HTTP ${response.code()}")
        }
    }

    suspend fun deleteAppointment(id: String): Result<Unit> = runCatching {
        val response = api.deleteAppointment(bearer(), id)
        if (response.isSuccessful) {
            removeLocal(id)
            Unit
        } else {
            error("${response.code()}")
        }
    }

    fun addLocal(appointment: Appointment) {
        cache.removeAll { it.id == appointment.id }
        cache.add(appointment)
        cache.sortBy { it.startTime }
    }

    fun removeLocal(id: String) {
        cache.removeAll { it.id == id }
    }
}
