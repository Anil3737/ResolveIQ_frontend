package com.simats.resolveiq_frontend.data.models

// ========================
// API Request Models
// ========================

data class RegisterRequest(
    val name: String,              // Changed from full_name to match backend
    val email: String,
    val password: String,
    val role: String = "EMPLOYEE", // Added with default value
    val department_id: Int? = null // Added to match backend schema
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class CreateTicketRequest(
    val title: String,
    val description: String,
    val department_id: Int,
    val sla_hours: Int = 24
)

data class UpdateTicketRequest(
    val status: String? = null,
    val priority: String? = null,
    val assigned_to: Int? = null,
    val team_id: Int? = null
)

data class ChangePasswordRequest(
    val old_password: String,
    val new_password: String
)

// ========================
// API Response Models
// ========================

data class LoginResponse(
    val access_token: String,
    val user: User      // Backend returns full user object
)

// UserResponse deleted - use User domain model instead

data class TicketListResponse(
    val total: Int,
    val limit: Int,
    val offset: Int,
    val tickets: List<Ticket>
)

data class CreateTicketResponse(
    val message: String,
    val ticket_id: Int,
    val status: String,
    val priority: String,
    val sla_deadline: String?
)

data class MessageResponse(
    val message: String
)

// Generic API Response Wrapper (matches Flask backend format)
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

data class ErrorResponse(
    val detail: String
)
