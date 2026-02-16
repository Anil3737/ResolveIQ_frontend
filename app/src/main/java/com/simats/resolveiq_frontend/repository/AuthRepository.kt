package com.simats.resolveiq_frontend.repository

import com.simats.resolveiq_frontend.api.AuthApiService
import com.simats.resolveiq_frontend.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val api: AuthApiService) {

    suspend fun register(request: RegisterRequest): Result<User> {
        return try {
            val response = api.register(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.success && response.data != null) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = api.getCurrentUser()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
