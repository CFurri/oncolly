package com.teknos.oncolly.entity

data class Doctor(
    val id: String,
    val firstName: String,
    val lastName: String,
    var email: String
    // Removing especialitat and hospital as they are not in the new provided API spec 
    // and user asked to "update everything".
)
