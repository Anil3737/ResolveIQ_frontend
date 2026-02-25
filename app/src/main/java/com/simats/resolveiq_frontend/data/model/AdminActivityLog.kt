package com.simats.resolveiq_frontend.data.model

enum class ActivitySeverity {
    INFO, WARNING, CRITICAL, SUCCESS
}

enum class ActivityType {
    USER_CREATED, USER_LOGIN, TICKET_CREATED, TICKET_ASSIGNED, 
    STATUS_UPDATED, AUTO_ESCALATED, SLA_BREACHED, 
    AUTO_CLOSED, MANUAL_CLOSED, ALL
}

data class AdminActivityLog(
    val id: Int,
    val user: String,
    val role: String,
    val action_type: String,
    val entity_type: String,
    val entity_id: Int?,
    val description: String,
    val created_at: String
)
