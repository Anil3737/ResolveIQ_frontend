package com.simats.resolveiq_frontend.api

import com.simats.resolveiq_frontend.data.models.ChangePasswordRequest
import com.simats.resolveiq_frontend.data.models.LoginRequest
import com.simats.resolveiq_frontend.data.models.LoginResponse
import com.simats.resolveiq_frontend.data.models.MessageResponse
import com.simats.resolveiq_frontend.data.models.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * API service interface for authentication endpoints
 */
interface AuthApiService {
    
    /**
     * Login with email and password
     * Returns access token and user information
     */
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    /**
     * Get current user information
     * Requires authentication (JWT token)
     */
    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(): UserResponse
    
    /**
     * Change password
     * Requires authentication
     */
    @POST("api/v1/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): MessageResponse
}
