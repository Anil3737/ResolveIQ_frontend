package com.simats.resolveiq_frontend.api

import com.simats.resolveiq_frontend.data.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<User>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    @GET("api/auth/me")
    suspend fun getCurrentUser(): ApiResponse<User>
}
