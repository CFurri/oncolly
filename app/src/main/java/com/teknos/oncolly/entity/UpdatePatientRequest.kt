package com.teknos.oncolly.entity

data class UpdatePatientRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val dateOfBirth: String
)
