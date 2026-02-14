package com.simats.resolveiq_frontend.data.models

data class User(
    val user_id: Int,
    val full_name: String,
    val email: String,
    val phone: String?,
    val role: String,  // ADMIN, TEAM_LEAD, AGENT, EMPLOYEE
    val is_active: Boolean
)
