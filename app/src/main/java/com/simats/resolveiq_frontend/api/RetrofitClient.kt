package com.simats.resolveiq_frontend.api

import android.content.Context
import com.simats.resolveiq_frontend.utils.UserPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.64.87.108:5000/"

    private var retrofit: Retrofit? = null

    private fun getClient(context: Context): Retrofit {
        if (retrofit == null) {

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(AuthInterceptor(UserPreferences(context)))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }
        return retrofit!!
    }

    fun getAuthApi(context: Context): AuthApiService {
        return getClient(context).create(AuthApiService::class.java)
    }

    fun getTicketApi(context: Context): TicketApiService {
        return getClient(context).create(TicketApiService::class.java)
    }

    fun getAdminApi(context: Context): AdminApiService {
        return getClient(context).create(AdminApiService::class.java)
    }
}
