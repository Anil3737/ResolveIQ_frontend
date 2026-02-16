package com.simats.resolveiq_frontend.repository

import android.util.Log
import com.simats.resolveiq_frontend.api.ResolveIQApi
import com.simats.resolveiq_frontend.data.models.*
import org.json.JSONObject

class AuthRepository(private val api: ResolveIQApi) {
    
    companion object {
        private const val TAG = "AuthRepository"
    }
    
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(email, password)
            
            // 🔍 DEBUG: Log request (excluding password)
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "LOGIN REQUEST")
            Log.d(TAG, "Email: $email")
            Log.d(TAG, "Password: [HIDDEN]")
            Log.d(TAG, "═══════════════════════════════════════")
            
            val response = api.login(request)
            
            // 🔍 DEBUG: Log response
            Log.d(TAG, "LOGIN RESPONSE")
            Log.d(TAG, "Status Code: ${response.code()}")
            Log.d(TAG, "Is Successful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null) {
                    Log.d(TAG, "Success: ${apiResponse.success}")
                    Log.d(TAG, "Message: ${apiResponse.message}")
                    
                    if (apiResponse.success && apiResponse.data != null) {
                        Log.d(TAG, "✅ Login successful!")
                        Log.d(TAG, "User: ${apiResponse.data.user.name} (${apiResponse.data.user.role})")
                        Log.d(TAG, "═══════════════════════════════════════")
                        Result.success(apiResponse.data)
                    } else {
                        val msg = apiResponse.message.ifEmpty { "Login failed with unknown error" }
                        Log.e(TAG, "❌ Login failed: $msg")
                        Log.d(TAG, "═══════════════════════════════════════")
                        Result.failure(Exception(msg))
                    }
                } else {
                    Log.e(TAG, "❌ Response body is null")
                    Result.failure(Exception("Login failed: Empty response"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ HTTP Error: ${response.code()}")
                Log.e(TAG, "Error Body: $errorBody")
                Log.d(TAG, "═══════════════════════════════════════")
                Result.failure(Exception("Login failed: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception during login", e)
            Log.d(TAG, "═══════════════════════════════════════")
            Result.failure(e)
        }
    }

    suspend fun register(
        name: String, 
        email: String, 
        password: String, 
        role: String = "EMPLOYEE", 
        departmentId: Int? = null
    ): Result<User> {
        return try {
            val request = RegisterRequest(
                name = name,
                email = email,
                password = password,
                role = role,
                department_id = departmentId
            )
            
            // 🔍 DEBUG: Log request (excluding password)
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "REGISTER REQUEST")
            Log.d(TAG, "Name: $name")
            Log.d(TAG, "Email: $email")
            Log.d(TAG, "Password: [HIDDEN]")
            Log.d(TAG, "Role: $role")
            Log.d(TAG, "Department ID: $departmentId")
            Log.d(TAG, "═══════════════════════════════════════")
            
            val response = api.register(request)
            
            // 🔍 DEBUG: Log response
            Log.d(TAG, "REGISTER RESPONSE")
            Log.d(TAG, "Status Code: ${response.code()}")
            Log.d(TAG, "Is Successful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null) {
                    Log.d(TAG, "Success: ${apiResponse.success}")
                    Log.d(TAG, "Message: ${apiResponse.message}")
                    
                    if (apiResponse.success && apiResponse.data != null) {
                        Log.d(TAG, "✅ Registration successful!")
                        Log.d(TAG, "User ID: ${apiResponse.data.id}")
                        Log.d(TAG, "User Name: ${apiResponse.data.name}")
                        Log.d(TAG, "═══════════════════════════════════════")
                        Result.success(apiResponse.data)
                    } else {
                        val msg = apiResponse.message.ifEmpty { "Registration failed with unknown error" }
                        Log.e(TAG, "❌ Registration failed: $msg")
                        Log.d(TAG, "═══════════════════════════════════════")
                        Result.failure(Exception(msg))
                    }
                } else {
                    Log.e(TAG, "❌ Response body is null")
                    Result.failure(Exception("Registration failed: Empty response"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ HTTP Error: ${response.code()}")
                Log.e(TAG, "Error Body: $errorBody")
                Log.d(TAG, "═══════════════════════════════════════")
                Result.failure(Exception("Registration failed: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception during registration", e)
            Log.d(TAG, "═══════════════════════════════════════")
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        return try {
            // TODO: Implement when backend /me endpoint is ready
            Result.failure(Exception("Not implemented"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
