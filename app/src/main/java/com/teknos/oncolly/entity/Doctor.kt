package com.teknos.oncolly.entity

data class Doctor(
    val id: String,
    val firstName: String,
    val lastName: String,
    var email: String,
    val specialization: String? = "Oncologia" // Default or nullable
)