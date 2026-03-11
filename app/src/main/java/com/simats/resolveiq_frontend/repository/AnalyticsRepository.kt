package com.simats.resolveiq_frontend.repository

import com.simats.resolveiq_frontend.api.AnalyticsApiService
import com.simats.resolveiq_frontend.data.model.*

class AnalyticsRepository(private val api: AnalyticsApiService) {

    suspend fun getSummary(): Result<AnalyticsSummary> {
        return try {
            val response = api.getSummary()
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch summary"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrend(days: Int = 14): Result<List<TrendAnalytics>> {
        return try {
            val response = api.getTrend(days)
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch trend"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSLACompliance(): Result<SLAAnalytics> {
        return try {
            val response = api.getSLACompliance()
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch SLA compliance"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
