package com.simats.resolveiq_frontend.api

import com.simats.resolveiq_frontend.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor to add JWT authorization token to all API requests
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getToken()
        
        val request = if (token != null) {
            // Add Authorization header with Bearer token
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            // No token, proceed with original request
            chain.request()
        }
        
        return chain.proceed(request)
    }
}
