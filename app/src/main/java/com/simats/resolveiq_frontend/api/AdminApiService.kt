package com.simats.resolveiq_frontend.api

import com.simats.resolveiq_frontend.data.model.ApiResponse
import com.simats.resolveiq_frontend.data.model.CreateStaffRequest
import com.simats.resolveiq_frontend.data.model.CreateTeamRequest
import com.simats.resolveiq_frontend.data.model.TeamData
import com.simats.resolveiq_frontend.data.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminApiService {

    @GET("api/admin/users")
    suspend fun getUsers(@Query("role") role: String? = null): ApiResponse<List<User>>

    @GET("api/admin/teams")
    suspend fun getTeams(): ApiResponse<List<TeamData>>

    @POST("api/admin/create-staff")
    suspend fun createStaff(@Body request: CreateStaffRequest): ApiResponse<User>

    @POST("api/admin/teams")
    suspend fun createTeam(@Body request: CreateTeamRequest): ApiResponse<Map<String, Any>>
    
    @POST("api/admin/create-agent")
    suspend fun createAgent(@Body request: com.simats.resolveiq_frontend.data.model.CreateAgentRequest): ApiResponse<User>

    @GET("api/admin/tickets")
    suspend fun getTickets(): ApiResponse<List<com.simats.resolveiq_frontend.data.model.Ticket>>

    @GET("api/admin/system-activity")
    suspend fun getSystemActivity(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("action_type") actionType: String? = null,
        @Query("user_id") userId: Int? = null,
        @Query("entity_type") entityType: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): com.simats.resolveiq_frontend.data.model.ActivityLogResponse

    @GET("api/admin/dashboard")
    suspend fun getDashboardData(): com.simats.resolveiq_frontend.data.model.AdminDashboardResponse

    @GET("api/admin/departments")
    suspend fun getDepartments(): ApiResponse<List<com.simats.resolveiq_frontend.data.model.Department>>

    @GET("api/admin/teams/{id}/members")
    suspend fun getTeamMembers(@Path("id") id: Int): ApiResponse<List<User>>
}
