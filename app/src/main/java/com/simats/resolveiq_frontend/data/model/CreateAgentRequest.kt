package com.simats.resolveiq_frontend.data.model

data class CreateAgentRequest(
    val full_name: String,
    val emp_id: String,
    val department: String,
    val team_lead_id: Int?,
    val location: String
)
