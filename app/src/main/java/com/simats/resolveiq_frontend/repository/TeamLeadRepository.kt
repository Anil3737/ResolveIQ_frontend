package com.simats.resolveiq_frontend.repository

import com.simats.resolveiq_frontend.api.TeamLeadApiService
import com.simats.resolveiq_frontend.data.model.TeamMember

class TeamLeadRepository(private val apiService: TeamLeadApiService) {
    suspend fun getTeamMembers(): List<TeamMember> {
        return try {
            val response = apiService.getTeamMembers()
            if (response.success) {
                response.data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
