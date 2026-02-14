package com.simats.resolveiq_frontend.api

import com.simats.resolveiq_frontend.utils.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton object to configure and provide Retrofit instance
 */
object RetrofitClient {
    
    @Volatile
    private var retrofit: Retrofit? = null
    
    /**
     * Get configured Retrofit instance
     * @param tokenManager Token manager for authentication
     * @return Configured Retrofit instance
     */
    fun getInstance(tokenManager: TokenManager): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: createRetrofit(tokenManager).also { retrofit = it }
        }
    }
    
    /**
     * Create Retrofit instance with interceptors
     */
    private fun createRetrofit(tokenManager: TokenManager): Retrofit {
        // Logging interceptor for debugging
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY  // Change to NONE in production
        }
        
        // Auth interceptor to add JWT token
        val authInterceptor = AuthInterceptor(tokenManager)
        
        // OkHttp client with interceptors and timeouts
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(ApiConstants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(ApiConstants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
        
        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Clear the Retrofit instance (useful for testing or logout)
     */
    fun clearInstance() {
        retrofit = null
    }
}
