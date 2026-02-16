package com.simats.resolveiq_frontend.api

import com.simats.resolveiq_frontend.data.models.ChangePasswordRequest
import com.simats.resolveiq_frontend.data.models.RegisterRequest
import com.simats.resolveiq_frontend.data.models.LoginRequest
import com.simats.resolveiq_frontend.data.models.LoginResponse
import com.simats.resolveiq_frontend.data.models.MessageResponse
import com.simats.resolveiq_frontend.data.models.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * API service interface for authentication endpoints
 */
interface AuthApiService {
    
    /**
     * Register new employee
     */
    @POST(ApiConstants.ENDPOINT_REGISTER)
    suspend fun register(@Body request: RegisterRequest): User

    /**
     * Login with email and password
     * Returns access token and user information
     */
    @POST(ApiConstants.ENDPOINT_LOGIN)
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    /**
     * Get current user information
     * Requires authentication (JWT token)
     */
    @GET(ApiConstants.ENDPOINT_ME)
    suspend fun getCurrentUser(): User
    
    /**
     * Change password
     * Requires authentication
     */
    @POST(ApiConstants.ENDPOINT_CHANGE_PASSWORD)
    suspend fun changePassword(@Body request: ChangePasswordRequest): MessageResponse
}