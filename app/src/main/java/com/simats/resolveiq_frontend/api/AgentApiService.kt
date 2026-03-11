package com.simats.resolveiq_frontend.api

import com.simats.resolveiq_frontend.data.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AgentApiService {
    @GET("api/agent/tickets")
    suspend fun getAgentTickets(): ApiResponse<List<Ticket>>

    @POST("api/agent/update-ticket")
    suspend fun updateTicketAction(@Body request: UpdateTicketActionRequest): ApiResponse<Ticket>
}
