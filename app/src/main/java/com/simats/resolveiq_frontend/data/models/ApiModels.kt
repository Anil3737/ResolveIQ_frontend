package com.simats.resolveiq_frontend.data.models

// ========================
// API Request Models
// ========================

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
    val token_type: String,
    val user: User
)

data class UserResponse(
    val user_id: Int,
    val full_name: String,
    val email: String,
    val phone: String?,
    val role: String,
    val is_active: Boolean
)

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

data class ErrorResponse(
    val detail: String
)
