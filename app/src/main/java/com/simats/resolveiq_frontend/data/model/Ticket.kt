package com.simats.resolveiq_frontend.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Ticket(
    @SerializedName("id")
    val id: Int,
    @SerializedName("ticket_number")
    val ticket_number: String?,   // Public display ID e.g. IQ-IT-2026-000001
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("priority")
    val priority: String,
    @SerializedName("department_id")
    val department_id: Int?,
    @SerializedName("department_name")
    val department_name: String?,
    @SerializedName("created_at")
    val created_at: String?,
    @SerializedName("breach_risk")
    val breach_risk: Int?,
    @SerializedName("ai_score")
    val ai_score: Int?,
    @SerializedName("sla_breached")
    val sla_breached: Boolean?,    // True if SLA deadline has passed and ticket is unresolved
    @SerializedName("created_by_name")
    val created_by_name: String?,
    @SerializedName("created_by_emp_id")
    val created_by_emp_id: String?,
    @SerializedName("sla_deadline")
    val sla_deadline: String?,
    @SerializedName("sla_hours")
    val sla_hours: Int? = null,
    @SerializedName("sla_remaining_seconds")
    val sla_remaining_seconds: Long?,
    @SerializedName("approved_at")
    val approved_at: String? = null,
    @SerializedName("accepted_at")
    val accepted_at: String? = null,
    @SerializedName("resolved_at")
    val resolved_at: String? = null,
    @SerializedName("closed_at")
    val closed_at: String? = null,
    @SerializedName("assigned_to")
    val assigned_to: Int? = null,
    @SerializedName("assigned_to_name")
    val assigned_to_name: String? = null,
    @SerializedName("can_accept")
    val can_accept: Boolean? = false,
    @SerializedName("can_decline")
    val can_decline: Boolean? = false,
    @SerializedName("can_resolve")
    val can_resolve: Boolean? = false,
    @SerializedName("ai_explanation")
    val ai_explanation: Map<String, Any>? = null
) : Serializable
