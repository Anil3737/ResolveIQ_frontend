package com.simats.resolveiq_frontend.data.model

data class RegisterRequest(
    val full_name: String,
    val email: String,
    val phone: String?,
    val password: String,
    val department_id: Int,
    val location: String? = null
)
