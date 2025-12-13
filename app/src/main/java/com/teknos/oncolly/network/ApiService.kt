package com.teknos.oncolly.network

import com.teknos.oncolly.entity.Pacient
import retrofit2.Response

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/patients")
    suspend fun getPacients(
        @Header("Authorization") token: String
    ): Response<List<Pacient>>
}