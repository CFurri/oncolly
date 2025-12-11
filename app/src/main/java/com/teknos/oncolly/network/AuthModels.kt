package com.teknos.oncolly.network

import com.google.gson.annotations.SerializedName

// Peticio
data class LoginRequest(
    val email: String,
    val password: String
)

// Resposta
data class LoginResponse(
    val id: Int,
    val role: String,
    val token: String
)