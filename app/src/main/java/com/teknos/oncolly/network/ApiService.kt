package com.teknos.oncolly.network

import com.teknos.oncolly.entity.Activity
import com.teknos.oncolly.entity.CreateActivityRequest
import com.teknos.oncolly.entity.Pacient
import com.teknos.oncolly.entity.Appointment
import com.teknos.oncolly.entity.CreateAppointmentRequest
import retrofit2.Response

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.DELETE

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/patients")
    suspend fun getPacients(
        @Header("Authorization") token: String
    ): Response<List<Pacient>>

    // --- GUARDAR ACTIVITATS ---
    @POST("api/activities")
    suspend fun createActivity(
        @Header("Authorization") token: String,
        @Body request: CreateActivityRequest
    ): Response<Void>

    @GET("/api/patients/{patientId}/activities")
    suspend fun getActivities(
        @Header("Authorization") token: String,
        @Path("patientId") patientId: String // <--- AFEGEIX AIXÒ
    ): Response<List<Activity>>

    // --- APPOINTMENTS (Doctor agenda) ---
    @GET("/api/appointments")
    suspend fun getAppointments(
        @Header("Authorization") token: String
    ): Response<List<Appointment>>

    @POST("/api/appointments")
    suspend fun createAppointment(
        @Header("Authorization") token: String,
        @Body request: CreateAppointmentRequest
    ): Response<Void>

    @DELETE("/api/appointments/{appointmentId}")
    suspend fun deleteAppointment(
        @Header("Authorization") token: String,
        @Path("appointmentId") appointmentId: String
    ): Response<Void>
}
