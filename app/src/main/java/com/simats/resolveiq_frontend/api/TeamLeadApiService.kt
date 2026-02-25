package com.simats.resolveiq_frontend.api

import com.simats.resolveiq_frontend.data.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Response

interface TeamLeadApiService {
    @GET("api/team-lead/team-members")
    suspend fun getTeamMembers(): ApiResponse<List<TeamMember>>

    @POST("api/team-lead/approve-ticket")
    suspend fun approveTicket(@Body request: ApproveTicketRequest): ApiResponse<Any>
}
