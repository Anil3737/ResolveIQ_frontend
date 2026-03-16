package com.simats.resolveiq_frontend.api

import android.content.Context
import com.simats.resolveiq_frontend.BuildConfig
import com.simats.resolveiq_frontend.utils.UserPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ── Single source of truth: URL is defined in ApiConstants, which reads
    //    from BuildConfig injected by build.gradle.kts at compile time.
    //    Debug  → http://10.210.228.108:5000/
    //    Release → https://api.resolveiq.com/
    //    To change the dev URL: edit debug.buildConfigField in app/build.gradle.kts

    private var retrofit: Retrofit? = null

    private fun getClient(context: Context): Retrofit {
        if (retrofit == null) {

            // Logging: full body only in debug — NEVER logs tokens/passwords in release
            val logging = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BODY
                else
                    HttpLoggingInterceptor.Level.NONE
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(AuthInterceptor(UserPreferences(context)))
                .connectTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                // Disable automatic retry — prevents OkHttp from silently
                // re-sending failed POST requests (which creates duplicates)
                .retryOnConnectionFailure(false)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(ApiConstants.BASE_URL)   // ← single source, no duplicate
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

    fun getTeamLeadApi(context: Context): TeamLeadApiService {
        return getClient(context).create(TeamLeadApiService::class.java)
    }

    fun getAgentApi(context: Context): AgentApiService {
        return getClient(context).create(AgentApiService::class.java)
    }

    fun getAnalyticsApi(context: Context): AnalyticsApiService {
        return getClient(context).create(AnalyticsApiService::class.java)
    }

    fun getSlaApi(context: Context): SlaApiService {
        return getClient(context).create(SlaApiService::class.java)
    }
}
