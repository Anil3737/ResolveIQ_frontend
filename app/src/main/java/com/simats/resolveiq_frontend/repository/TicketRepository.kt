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
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTicket(request: CreateTicketRequest): Result<Unit> {
        return try {
            val response = api.createTicket(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to create ticket"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
