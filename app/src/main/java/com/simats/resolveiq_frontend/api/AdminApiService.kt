package com.simats.resolveiq_frontend.api

import com.simats.resolveiq_frontend.data.model.ApiResponse
import com.simats.resolveiq_frontend.data.model.CreateStaffRequest
import com.simats.resolveiq_frontend.data.model.TeamData
import com.simats.resolveiq_frontend.data.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AdminApiService {

    @GET("api/admin/users")
    suspend fun getUsers(@Query("role") role: String? = null): ApiResponse<List<User>>

    @GET("api/admin/teams")
    suspend fun getTeams(): ApiResponse<List<TeamData>>

    @POST("api/admin/create-staff")
    suspend fun createStaff(@Body request: CreateStaffRequest): ApiResponse<User>
    
    @POST("api/admin/create-agent")
    suspend fun createAgent(@Body request: Map<String, Any>): ApiResponse<User>
}
