package com.simats.resolveiq_frontend.data.model

import com.google.gson.annotations.SerializedName

data class AdminDashboardResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("metrics") val metrics: DashboardMetrics,
    @SerializedName("risk_distribution") val riskDistribution: RiskDistribution,
    @SerializedName("top_risky_tickets") val topRiskyTickets: List<DashboardTicket>,
    @SerializedName("message") val message: String? = null
)

data class DashboardMetrics(
    @SerializedName("total_tickets") val totalTickets: Int,
    @SerializedName("high_risk") val highRisk: Int,
    @SerializedName("sla_breached") val slaBreached: Int,
    @SerializedName("escalated") val escalated: Int
)

data class RiskDistribution(
    @SerializedName("critical") val critical: Int,
    @SerializedName("high") val high: Int,
    @SerializedName("medium") val medium: Int,
    @SerializedName("low") val low: Int
)

data class DashboardTicket(
    @SerializedName("id") val id: Int,
    @SerializedName("ticket_number") val ticketNumber: String?,
    @SerializedName("title") val title: String,
    @SerializedName("status") val status: String,
    @SerializedName("ai_score") val aiScore: Int
)
