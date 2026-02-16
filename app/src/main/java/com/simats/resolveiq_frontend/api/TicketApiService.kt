package com.simats.resolveiq_frontend.api
import com.simats.resolveiq_frontend.data.model.CreateTicketRequest

import com.simats.resolveiq_frontend.data.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TicketApiService {

    @GET("api/tickets")
    suspend fun getTickets(): ApiResponse<List<Ticket>>

    @POST("api/tickets")
    suspend fun createTicket(@Body request: CreateTicketRequest): ApiResponse<Ticket>
}
