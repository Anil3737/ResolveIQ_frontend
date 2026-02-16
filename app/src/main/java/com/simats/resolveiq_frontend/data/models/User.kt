package com.simats.resolveiq_frontend.data.models

/**
 * User data class matching backend contract EXACTLY
 * Backend fields: id, name, email, role, department_id, created_at
 */
data class User(
    val id: Int,
    val name: String,              // ✅ Backend uses 'name'
    val email: String,
    val role: String,              // ADMIN, TEAM_LEAD, AGENT, EMPLOYEE
    val department_id: Int? = null,
    val created_at: String? = null
)
