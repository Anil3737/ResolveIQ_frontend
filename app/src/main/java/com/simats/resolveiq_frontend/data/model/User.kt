package com.simats.resolveiq_frontend.data.model

data class User(
    val id: Int,
    val full_name: String,
    val email: String,
    val role: String,
    val location: String? = null,
    val phone: String? = null
)
