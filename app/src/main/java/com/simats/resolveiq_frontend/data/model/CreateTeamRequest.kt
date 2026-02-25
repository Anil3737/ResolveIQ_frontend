package com.simats.resolveiq_frontend.data.model

data class CreateTeamRequest(
    val name: String,
    val description: String,
    val department_id: Int,
    val team_lead_id: Int,
    val agent_ids: List<Int> = emptyList()
)
