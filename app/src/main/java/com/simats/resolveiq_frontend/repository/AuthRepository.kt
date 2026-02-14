package com.simats.resolveiq_frontend.repository

import com.simats.resolveiq_frontend.api.AuthApiService
import com.simats.resolveiq_frontend.data.models.ChangePasswordRequest
import com.simats.resolveiq_frontend.data.models.LoginRequest
import com.simats.resolveiq_frontend.data.models.User
import com.simats.resolveiq_frontend.data.models.UserResponse
import com.simats.resolveiq_frontend.utils.TokenManager
import com.simats.resolveiq_frontend.utils.UserPreferences

/**
 * Repository for authentication operations
 * Handles API calls and local data persistence
 */
class AuthRepository(
    private val apiService: AuthApiService,
    private val tokenManager: TokenManager,
    private val userPreferences: UserPreferences
) {
    
    /**
     * Login with email and password
     * @return Result with User object or error
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            
            // Save token and user information
            tokenManager.saveToken(response.access_token)
            
            // Convert UserResponse to User model
            val user = User(
                user_id = response.user.user_id,
                full_name = response.user.full_name,
                email = response.user.email,
                phone = response.user.phone,
                role = response.user.role,
                is_active = response.user.is_active
            )
            
            userPreferences.saveUser(user)
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current user information from API
     */
    suspend fun getCurrentUser(): Result<UserResponse> {
        return try {
            val user = apiService.getCurrentUser()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Change user password
     */
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<String> {
        return try {
            val request = ChangePasswordRequest(oldPassword, newPassword)
            val response = apiService.changePassword(request)
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Logout - clear token and user data
     */
    suspend fun logout() {
        tokenManager.clearToken()
        userPreferences.clearUser()
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.hasToken()
    }
    
    /**
     * Get cached user from local storage
     */
    suspend fun getCachedUser(): User? {
        return userPreferences.getUser()
    }
}
