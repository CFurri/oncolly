package com.teknos.oncolly.entity

/**
 * Doctor agenda appointment returned by the backend.
 * Times are ISO-8601 strings (e.g., 2025-01-05T10:00:00).
 */
data class Appointment(
    val id: String,
    val doctorId: String?,
    val doctorName: String?,
    val patientId: String,
    val patientName: String?,
    val startTime: String,
    val endTime: String,
    val status: String?,
    val title: String
)

/** Request body to create a new appointment. */
data class CreateAppointmentRequest(
    val id: String,
    val patientId: String,
    val startTime: String,
    val endTime: String,
    val title: String,
    val doctorNotes: String? = null
)
