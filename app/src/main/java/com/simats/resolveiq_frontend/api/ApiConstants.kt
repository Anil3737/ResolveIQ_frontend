package com.simats.resolveiq_frontend.api

import com.simats.resolveiq_frontend.BuildConfig

object ApiConstants {
    /**
     * BASE_URL is injected at compile time by build.gradle.kts:
     *   • Debug build   → http://10.210.228.108:5000/  (local Flask server — update IP via 'ipconfig')
     *   • Release build → https://api.resolveiq.com/   (production cloud server)
     *
     * To switch the dev IP: edit the debug buildConfigField in app/build.gradle.kts
     * — do NOT hardcode any URL in this file.
     */
    val BASE_URL: String = BuildConfig.BASE_URL

    const val TIMEOUT_SECONDS = 30L

    // Auth endpoints
    const val ENDPOINT_LOGIN = "api/auth/login"
    const val ENDPOINT_REGISTER = "api/auth/register"
    const val ENDPOINT_ME = "api/auth/me"
    const val ENDPOINT_CHANGE_PASSWORD = "api/auth/change-password"

    // Ticket endpoints
    const val ENDPOINT_TICKETS = "api/tickets"
}
