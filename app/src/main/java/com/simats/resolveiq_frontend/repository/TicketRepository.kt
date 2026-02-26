package com.simats.resolveiq_frontend.repository

import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.api.TicketApiService
import com.simats.resolveiq_frontend.data.model.Ticket
import com.simats.resolveiq_frontend.data.model.CreateTicketRequest
import com.simats.resolveiq_frontend.data.model.TicketDetailResponse
import com.simats.resolveiq_frontend.data.model.ApiResponse
import com.simats.resolveiq_frontend.data.model.ApproveTicketRequest
import com.simats.resolveiq_frontend.data.model.AssignTicketRequest
import com.simats.resolveiq_frontend.data.model.TeamMember
import com.simats.resolveiq_frontend.data.model.UpdateTicketActionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

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

    suspend fun createTicket(request: CreateTicketRequest): Result<Int> {
        return try {
            val response = api.createTicket(request)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data.id)
                } else {
                    Result.failure(Exception(apiResponse?.message ?: "Failed to create ticket"))
                }
            } else {
                val errorMsg = try {
                    val json = JSONObject(response.errorBody()?.string() ?: "")
                    json.optString("message", "Failed to create ticket")
                } catch (e: Exception) {
                    "Failed to create ticket"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTicketDetails(id: Int): Result<TicketDetailResponse> {
        return try {
            val response = api.getTicketDetails(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch ticket details"))
                }
            } else {
                Result.failure(Exception("Failed to fetch ticket details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveTicket(context: android.content.Context, ticketId: Int): Result<Unit> {
        return try {
            val teamLeadApi = RetrofitClient.getTeamLeadApi(context)
            val response = teamLeadApi.approveTicket(ApproveTicketRequest(ticketId))
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to approve ticket"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTicketAction(context: android.content.Context, ticketId: Int, action: String): Result<Ticket> {
        return try {
            val agentApi = RetrofitClient.getAgentApi(context)
            val response = agentApi.updateTicketAction(UpdateTicketActionRequest(ticketId, action))
            if (response.success && response.data != null) {
                Result.success(response.data!!)
            } else {
                Result.failure(Exception(response.message ?: "Failed to $action ticket"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignTicket(context: android.content.Context, ticketId: Int, agentId: Int): Result<Unit> {
        return try {
            val teamLeadApi = RetrofitClient.getTeamLeadApi(context)
            val response = teamLeadApi.assignTicket(AssignTicketRequest(ticketId, agentId))
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to assign ticket"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTeamMembers(context: android.content.Context): Result<List<TeamMember>> {
        return try {
            val teamLeadApi = RetrofitClient.getTeamLeadApi(context)
            val response = teamLeadApi.getTeamMembers()
            if (response.success) {
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch team members"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
