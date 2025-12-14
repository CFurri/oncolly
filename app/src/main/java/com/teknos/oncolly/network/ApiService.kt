package com.teknos.oncolly.network

import com.teknos.oncolly.entity.Activity
import com.teknos.oncolly.entity.CreateActivityRequest
import com.teknos.oncolly.entity.Pacient
import com.teknos.oncolly.entity.Appointment
import com.teknos.oncolly.entity.CreateAppointmentRequest
import com.teknos.oncolly.entity.Doctor
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

    // --- GUARDAR ACTIVITATS (Pacient activitats)---
    @POST("api/activities")
    suspend fun createActivity(
        @Header("Authorization") token: String,
        @Body request: CreateActivityRequest
    ): Response<Void>

    // --- PEL PACIENT --> permet veure les seves propies activitats ---
    @GET("api/activities")
    suspend fun getMyActivities(
        @Header("Authorization") token: String
    ): Response<List<Activity>>

    // --- Per al Doctor --> Permet veure les activitats del pacient que vulgui ---
    @GET("/api/patients/{patientId}/activities")
    suspend fun getActivities(
        @Header("Authorization") token: String,
        @Path("patientId") patientId: String
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

    // Per obtenir les dades del pacient actual (necessari per omplir el Singleton)
    @GET("api/patients/{id}")
    suspend fun getPacientProfile(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Pacient>

    // Per obtenir les dades del doctor i omplir el Singleton ---@GET("api/doctors/{id}")
    suspend fun getDoctorProfile(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Doctor>

    // --- DELETE Activity (pacient) ---
    @DELETE("api/activities/{id}")
    suspend fun deleteActivity(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ) : Response<Void>
}
