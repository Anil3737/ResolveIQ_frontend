package com.simats.resolveiq_frontend.api

import com.simats.resolveiq_frontend.data.model.*
import retrofit2.http.Body
import retrofit2.http.POST

interface AgentApiService {
    @POST("api/agent/update-ticket")
    suspend fun updateTicketAction(@Body request: UpdateTicketActionRequest): ApiResponse<Ticket>
}
