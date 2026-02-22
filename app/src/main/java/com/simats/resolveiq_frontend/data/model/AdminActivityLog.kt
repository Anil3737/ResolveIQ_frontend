package com.simats.resolveiq_frontend.data.model

enum class ActivitySeverity {
    INFO, WARNING, CRITICAL, SUCCESS
}

enum class ActivityType {
    USER_ACTION, ESCALATION, SLA_BREACH, AI_EVENT, MAJOR_INCIDENT, ALL
}

data class AdminActivityLog(
    val id: String,
    val type: ActivityType,
    val title: String,
    val description: String,
    val timestamp: String,
    val severity: ActivitySeverity,
    val teamName: String? = null
)
