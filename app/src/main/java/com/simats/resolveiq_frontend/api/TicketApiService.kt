package com.simats.resolveiq_frontend.api

import com.simats.resolveiq_frontend.data.models.CreateTicketRequest
import com.simats.resolveiq_frontend.data.models.CreateTicketResponse
import com.simats.resolveiq_frontend.data.models.MessageResponse
import com.simats.resolveiq_frontend.data.models.Ticket
import com.simats.resolveiq_frontend.data.models.TicketListResponse
import com.simats.resolveiq_frontend.data.models.UpdateTicketRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API service interface for ticket endpoints
 */
interface TicketApiService {
    
    /**
     * Get list of tickets with optional filters
     * @param status Filter by status (OPEN, IN_PROGRESS, RESOLVED, CLOSED)
     * @param priority Filter by priority (P1, P2, P3, P4)
     * @param departmentId Filter by department ID
     * @param search Search in title/description
     * @param limit Number of results to return
     * @param offset Pagination offset
     */
    @GET("api/v1/tickets")
    suspend fun getTickets(
        @Query("status") status: String? = null,
        @Query("priority") priority: String? = null,
        @Query("department_id") departmentId: Int? = null,
        @Query("search") search: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): TicketListResponse
    
    /**
     * Get single ticket by ID
     */
    @GET("api/v1/tickets/{id}")
    suspend fun getTicket(@Path("id") id: Int): Ticket
    
    /**
     * Create new ticket
     */
    @POST("api/v1/tickets")
    suspend fun createTicket(@Body request: CreateTicketRequest): CreateTicketResponse
    
    /**
     * Update ticket (status, priority, assignment)
     */
    @PATCH("api/v1/tickets/{id}")
    suspend fun updateTicket(
        @Path("id") id: Int,
        @Body request: UpdateTicketRequest
    ): MessageResponse
    
    /**
     * Delete/close ticket
     */
    @DELETE("api/v1/tickets/{id}")
    suspend fun deleteTicket(@Path("id") id: Int): MessageResponse
}
