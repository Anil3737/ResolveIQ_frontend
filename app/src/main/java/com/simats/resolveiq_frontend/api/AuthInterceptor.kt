package com.simats.resolveiq_frontend.api

import com.simats.resolveiq_frontend.utils.UserPreferences
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor to add JWT authorization token to all API requests
 */
class AuthInterceptor(private val userPreferences: UserPreferences) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = userPreferences.getToken()
        
        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}
