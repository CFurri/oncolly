package com.teknos.oncolly.entity

data class Activity(
    val id: String,
    val activityType: String, // "walking", "medication", etc.
    val value: String,        // "30 min", "Ibuprofè"
    val occurredAt: String,   // Data
    val patientId: String?    // El ID del pacient que l'ha fet (si el servidor l'envia)
)