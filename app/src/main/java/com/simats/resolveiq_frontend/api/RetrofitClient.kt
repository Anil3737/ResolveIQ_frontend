package com.simats.resolveiq_frontend.api

import android.content.Context
import com.simats.resolveiq_frontend.api.ResolveIQApi
import com.simats.resolveiq_frontend.utils.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// AuthInterceptor moved to separate file

object RetrofitClient {
    private const val BASE_URL = "http://10.64.87.108:5000/"

    fun getApi(context: Context): ResolveIQApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(TokenManager(context)))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ResolveIQApi::class.java)
    }
}
