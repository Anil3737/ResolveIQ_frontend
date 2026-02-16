package com.simats.resolveiq_frontend.repository

import com.simats.resolveiq_frontend.api.TicketApiService
import com.simats.resolveiq_frontend.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TicketRepository(private val api: TicketApiService) {

    suspend fun getTickets(): Result<List<Ticket>> {
        return try {
            val response = api.getTickets()
            if (response.success) {
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch tickets"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTicket(request: CreateTicketRequest): Result<Ticket> {
        return try {
            val response = api.createTicket(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to create ticket"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
