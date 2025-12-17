package com.teknos.oncolly.singletons

import com.teknos.oncolly.entity.CreatePatientRequest
import com.teknos.oncolly.entity.Pacient
import com.teknos.oncolly.network.ApiService

/**
 * Singleton repository to centralize patient calls and keep a small in-memory cache.
 */
object PatientSingleton {
    private val api: ApiService = SingletonApp.getInstance().api
    private val cache: MutableList<Pacient> = mutableListOf()

    private fun bearer(): String =
        SingletonApp.getInstance().userToken?.let { "Bearer $it" }
            ?: error("Missing auth token")

    suspend fun fetchPatients(): Result<List<Pacient>> = runCatching {
        val response = api.getPacients(bearer())
        if (response.isSuccessful) {
            val body = response.body().orEmpty()
            cache.clear()
            cache.addAll(body)
            body
        } else {
            error("${response.code()}")
        }
    }

    fun cached(): List<Pacient> = cache.toList()

    suspend fun createPatient(request: CreatePatientRequest): Result<Unit> = runCatching {
        val response = api.createPatient(bearer(), request)
        if (response.isSuccessful) {
            Unit
        } else {
            val errorBody = response.errorBody()?.string()
            error(errorBody?.ifBlank { "HTTP ${response.code()}" } ?: "HTTP ${response.code()}")
        }
    }

    suspend fun deletePatient(id: String): Result<Unit> = runCatching {
        val response = api.deletePatient(bearer(), id)
        if (response.isSuccessful) {
            removeLocal(id)
            Unit
        } else {
            error("${response.code()}")
        }
    }

    fun addLocal(patient: Pacient) {
        cache.removeAll { it.id == patient.id }
        cache.add(patient)
        cache.sortBy { it.firstName }
    }

    fun removeLocal(id: String) {
        cache.removeAll { it.id == id }
    }
}
