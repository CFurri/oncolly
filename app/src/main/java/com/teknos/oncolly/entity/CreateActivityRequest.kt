package com.teknos.oncolly.entity

data class CreateActivityRequest(
    val id: String,          // UUID
    val activityType: String,
    val value: String,
    val occurredAt: String   // Data
)