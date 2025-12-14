package com.teknos.oncolly.entity

data class Pacient (
    val id: String,
    val firstName: String,
    val lastName: String,
    var email: String,
    val phoneNumber: String?,
    val dateOfBirth: String?
)