package com.teknos.oncolly.entity

data class CreatePatientRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val dateOfBirth: String
)