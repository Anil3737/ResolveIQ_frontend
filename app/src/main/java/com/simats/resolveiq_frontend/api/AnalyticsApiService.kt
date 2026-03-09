package com.simats.resolveiq_frontend.api

import com.simats.resolveiq_frontend.data.model.*
import retrofit2.http.GET
import retrofit2.http.Query

interface AnalyticsApiService {
    @GET("api/analytics/summary")
    suspend fun getSummary(): AnalyticsResponse<AnalyticsSummary>

    @GET("api/analytics/by-department")
    suspend fun getByDepartment(): AnalyticsResponse<List<DepartmentAnalytics>>

    @GET("api/analytics/trend")
    suspend fun getTrend(@Query("days") days: Int = 14): AnalyticsResponse<List<TrendAnalytics>>

    @GET("api/analytics/agent-performance")
    suspend fun getAgentPerformance(): AnalyticsResponse<List<AgentPerformance>>

    @GET("api/analytics/sla-compliance")
    suspend fun getSLACompliance(): AnalyticsResponse<SLAAnalytics>

    @GET("api/analytics/feedback-summary")
    suspend fun getFeedbackSummary(): AnalyticsResponse<FeedbackSummary>
}
