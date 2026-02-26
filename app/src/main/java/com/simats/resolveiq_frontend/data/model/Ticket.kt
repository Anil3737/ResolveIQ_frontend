package com.simats.resolveiq_frontend.data.model

import java.io.Serializable

data class Ticket(
    val id: Int,
    val ticket_number: String?,   // Public display ID e.g. IQ-IT-2026-000001
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    val department_id: Int?,
    val department_name: String?,
    val created_at: String?,
    val breach_risk: Int?,
    val ai_score: Int?,
    val sla_breached: Boolean?,    // True if SLA deadline has passed and ticket is unresolved
    val created_by_name: String?,
    val created_by_emp_id: String?,
    val sla_deadline: String?,
    val sla_remaining_seconds: Long?,
    val approved_at: String? = null,
    val accepted_at: String? = null,
    val resolved_at: String? = null,
    val closed_at: String? = null,
    val assigned_to: Int? = null,
    val assigned_to_name: String? = null,
    val can_accept: Boolean? = false,
    val can_decline: Boolean? = false,
    val can_resolve: Boolean? = false
) : Serializable
