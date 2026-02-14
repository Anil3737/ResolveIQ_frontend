package com.simats.resolveiq_frontend.api

object ApiConstants {
    // For Android Emulator: use 10.0.2.2 to access host machine's localhost
    // For physical device on same network: use actual IP address (e.g., "192.168.1.100:8000")
    const val BASE_URL = "http://10.0.2.2:8000/"
    
    const val TIMEOUT_SECONDS = 30L
    
    // Endpoints
    const val API_VERSION = "api/v1"
    
    // Auth endpoints
    const val ENDPOINT_LOGIN = "$API_VERSION/auth/login"
    const val ENDPOINT_ME = "$API_VERSION/auth/me"
    const val ENDPOINT_CHANGE_PASSWORD = "$API_VERSION/auth/change-password"
    
    // Ticket endpoints
    const val ENDPOINT_TICKETS = "$API_VERSION/tickets"
}
