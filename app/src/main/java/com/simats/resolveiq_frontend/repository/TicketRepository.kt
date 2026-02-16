package com.simats.resolveiq_frontend.repository

import com.simats.resolveiq_frontend.api.ResolveIQApi
import com.simats.resolveiq_frontend.data.models.*

class TicketRepository(private val api: ResolveIQApi) {
    
    suspend fun getTickets(
        status: String? = null,
        priority: String? = null,
        search: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Result<TicketListResponse> {
        return try {
            val response = api.getTickets(status, priority, search, limit, offset)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Failed to fetch tickets: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createTicket(request: CreateTicketRequest): Result<CreateTicketResponse> {
        return try {
            val response = api.createTicket(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Failed to create ticket: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
