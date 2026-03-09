package com.simats.resolveiq_frontend.data.model

data class TeamPerformanceStats(
    val totalMembers: Int,
    val totalActiveTickets: Int,
    val totalResolvedToday: Int,
    val totalCapacity: Int,
    val avgLoadPercent: Int,
    val slaRiskCount: Int
)
