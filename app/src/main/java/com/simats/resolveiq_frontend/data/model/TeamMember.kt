package com.simats.resolveiq_frontend.data.model

data class TeamMember(
    val id: Int,
    val full_name: String,
    val email: String,
    val department: String,
    val location: String,
    val active_tickets: Int,
    val resolved_today: Int,
    val daily_capacity: Int
)
