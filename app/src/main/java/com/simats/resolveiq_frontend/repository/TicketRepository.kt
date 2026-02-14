package com.simats.resolveiq_frontend.repository

import com.simats.resolveiq_frontend.api.TicketApiService
import com.simats.resolveiq_frontend.data.models.CreateTicketRequest
import com.simats.resolveiq_frontend.data.models.CreateTicketResponse
import com.simats.resolveiq_frontend.data.models.Ticket
import com.simats.resolveiq_frontend.data.models.TicketListResponse
import com.simats.resolveiq_frontend.data.models.UpdateTicketRequest

/**
 * Repository for ticket operations
 * Handles API calls for ticket management
 */
class TicketRepository(private val apiService: TicketApiService) {
    
    /**
     * Get list of tickets with filters
     */
    suspend fun getTickets(
        status: String? = null,
        priority: String? = null,
        departmentId: Int? = null,
        search: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Result<TicketListResponse> {
        return try {
            val response = apiService.getTickets(
                status, priority, departmentId, search, limit, offset
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get single ticket by ID
     */
    suspend fun getTicket(ticketId: Int): Result<Ticket> {
        return try {
            val ticket = apiService.getTicket(ticketId)
            Result.success(ticket)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create new ticket
     */
    suspend fun createTicket(
        title: String,
        description: String,
        departmentId: Int,
        slaHours: Int = 24
    ): Result<CreateTicketResponse> {
        return try {
            val request = CreateTicketRequest(title, description, departmentId, slaHours)
            val response = apiService.createTicket(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update ticket status
     */
    suspend fun updateTicketStatus(ticketId: Int, status: String): Result<String> {
        return try {
            val request = UpdateTicketRequest(status = status)
            val response = apiService.updateTicket(ticketId, request)
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update ticket priority
     */
    suspend fun updateTicketPriority(ticketId: Int, priority: String): Result<String> {
        return try {
            val request = UpdateTicketRequest(priority = priority)
            val response = apiService.updateTicket(ticketId, request)
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update ticket assignment
     */
    suspend fun updateTicketAssignment(ticketId: Int, assignedTo: Int): Result<String> {
        return try {
            val request = UpdateTicketRequest(assigned_to = assignedTo)
            val response = apiService.updateTicket(ticketId, request)
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete/close ticket
     */
    suspend fun deleteTicket(ticketId: Int): Result<String> {
        return try {
            val response = apiService.deleteTicket(ticketId)
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
