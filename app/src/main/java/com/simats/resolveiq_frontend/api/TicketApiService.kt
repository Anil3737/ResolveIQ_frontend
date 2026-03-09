package com.simats.resolveiq_frontend.api
import com.simats.resolveiq_frontend.data.model.CreateTicketRequest

import com.simats.resolveiq_frontend.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TicketApiService {

    @GET("api/tickets")
    suspend fun getTickets(@retrofit2.http.Query("limit") limit: Int? = null): ApiResponse<List<Ticket>>

    @POST("api/tickets")
    suspend fun createTicket(@Body request: CreateTicketRequest): Response<ApiResponse<Ticket>>

    @GET("api/tickets/{id}")
    suspend fun getTicketDetails(@retrofit2.http.Path("id") id: Int): Response<TicketDetailResponse>

    @POST("api/tickets/{id}/feedback")
    suspend fun submitFeedback(
        @retrofit2.http.Path("id") id: Int,
        @Body request: FeedbackRequest
    ): Response<FeedbackResponse>

    @GET("api/tickets/{id}/feedback")
    suspend fun getFeedback(
        @retrofit2.http.Path("id") id: Int
    ): Response<FeedbackResponse>
}
