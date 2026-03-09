package com.simats.resolveiq_frontend.data.model

data class AnalyticsSummary(
    val total_tickets: Int,
    val status_summary: Map<String, Int>,
    val priority_summary: Map<String, Int>
)

data class DepartmentAnalytics(
    val department: String,
    val total: Int,
    val open: Int,
    val resolved: Int
)

data class TrendAnalytics(
    val date: String,
    val tickets: Int
)

data class AgentPerformance(
    val agent: String,
    val emp_id: String,
    val assigned: Int,
    val resolved: Int,
    val resolution_rate: Double,
    val avg_resolution_hours: Double?
)

data class SLADepartmentBreakdown(
    val department: String,
    val total: Int,
    val met: Int,
    val breached: Int,
    val compliance_pct: Double
)

data class SLAAnalytics(
    val total_with_sla: Int,
    val met: Int,
    val breached: Int,
    val compliance_pct: Double,
    val by_department: List<SLADepartmentBreakdown>
)

data class AnalyticsResponse<T>(
    val success: Boolean,
    val data: T,
    val message: String? = null
)

data class FeedbackSummary(
    val total_feedbacks: Int,
    val avg_rating: Double,
    val rating_distribution: Map<String, Int>,
    val top_suggestions: Map<String, Int>
)
