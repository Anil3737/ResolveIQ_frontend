package com.simats.resolveiq_frontend.api

object ApiConstants {
    // CRITICAL for Physical Device: 10.0.2.2 ONLY works for emulators.
    // To test on your phone: 
    // 1. Run 'ipconfig' in cmd on your PC to get your IPv4 address (e.g., 192.168.1.5).
    // 2. Change the string below to: "http://YOUR_PC_IP:5000/"
    // 3. Ensure your phone and PC are on the SAME Wi-Fi network.
    const val BASE_URL = "http://10.64.87.108:5000/"  // Changed from 8000 to 5000 for Flask
    
    const val TIMEOUT_SECONDS = 30L
    
    // Auth endpoints - removed API_VERSION prefix to match Flask routes
    const val ENDPOINT_LOGIN = "api/auth/login"
    const val ENDPOINT_REGISTER = "api/auth/register"
    const val ENDPOINT_ME = "api/auth/me"
    const val ENDPOINT_CHANGE_PASSWORD = "api/auth/change-password"
    
    // Ticket endpoints
    const val ENDPOINT_TICKETS = "api/tickets"
}
