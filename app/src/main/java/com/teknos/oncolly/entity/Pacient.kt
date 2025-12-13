package com.teknos.oncolly.entity

data class Pacient (
    val id: String,
    var email: String,
    val phoneNumber: String?,
    val dateOfBirth: String?
)
/*
Quan fas una data class en comptes de class a seques,
Kotlin escriu 4 funcions automàticament:
    toString(), equals(), hashCode() i copy()

Si fiquem val és com ficar un getter
Si fiquem var és com ficar un getter i un setter
*/

