package com.simats.resolveiq_frontend.data.model

data class CreateStaffRequest(
    val full_name: String,
    val emp_id: String,
    val department: String,
    val location: String,
    val role: String
)
