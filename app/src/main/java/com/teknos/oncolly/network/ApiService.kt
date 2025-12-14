package com.teknos.oncolly.network

import com.teknos.oncolly.entity.Activity
import com.teknos.oncolly.entity.CreateActivityRequest
import com.teknos.oncolly.entity.Doctor
import com.teknos.oncolly.entity.Pacient
import retrofit2.Response

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // Per obtenir les dades d'UN sol pacient
    @GET("api/patients/{id}")
    suspend fun getPacientProfile(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Pacient>

    // Per obtenir les dades del doctor
    @GET("api/doctors/{id}")
    suspend fun getDoctorProfile(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Doctor>

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

    @GET("/api/activities/patient/{patientId}")
    suspend fun getActivities(
        @Header("Authorization") token: String,
        @Path("patientId") patientId: String
    ): Response<List<Activity>>

    // --- ESBORRAR ACTIVITATS ---
    @DELETE("/api/activities/{activityId}")
    suspend fun deleteActivity(
        @Header("Authorization") token: String,
        @Path("activityId") activityId: String
    ): Response<Void>
}