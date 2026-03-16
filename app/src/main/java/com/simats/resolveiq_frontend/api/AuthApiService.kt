package com.simats.resolveiq_frontend.api

import com.simats.resolveiq_frontend.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<User>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    @GET("api/auth/me")
    suspend fun getCurrentUser(): ApiResponse<User>

    @POST("api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): ApiResponse<Unit>

    @GET("api/auth/check-id")
    suspend fun checkEmployeeIdExists(@Query("emp_id") empId: String): ApiResponse<Map<String, Boolean>>

    @POST("api/auth/request-password-reset")
    suspend fun requestPasswordReset(@retrofit2.http.Body request: PasswordResetRequest): ApiResponse<Unit>

    @POST("api/auth/check-reset-password")
    suspend fun checkPasswordReset(@retrofit2.http.Body request: PasswordResetRequest): retrofit2.Response<CheckPasswordResetResponse>
}
