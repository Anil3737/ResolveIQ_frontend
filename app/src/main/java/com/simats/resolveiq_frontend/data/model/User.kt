package com.simats.resolveiq_frontend.data.model

data class User(
    val id: Int,
    val full_name: String,
    val email: String,
    val role: String,
    val location: String? = null,
    val phone: String? = null,
    val department_name: String? = null,
    val team_lead_name: String? = null,
    val joining_date: String? = null
)
