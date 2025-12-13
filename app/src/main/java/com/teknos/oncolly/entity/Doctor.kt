package com.teknos.oncolly.entity

data class Doctor(
    val id: String,          // Canviat a String per coherència amb el login
    var nom: String,
    val especialitat: String, // Ex: "Oncologia", "Medicina General"
    var hospital: String,
    var email: String
)

/*
Quan fas una data class en comptes de class a seques,
Kotlin escriu 4 funcions automàticament:
    toString(), equals(), hashCode() i copy()

Si fiquem val és com ficar un getter
Si fiquem var és com ficar un getter i un setter
*/