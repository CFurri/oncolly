package com.teknos.oncolly.network

import com.google.gson.annotations.SerializedName

// Peticio
data class LoginRequest(
    val email: String,
    val password: String
)

// Resposta
data class LoginResponse(
    val token: String,
    val role: String,
    val userId: String
)