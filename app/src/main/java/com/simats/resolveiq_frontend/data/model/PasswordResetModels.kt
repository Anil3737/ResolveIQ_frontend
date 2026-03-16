package com.simats.resolveiq_frontend.data.model

data class PasswordResetRequest(
    val email: String,
    val emp_id: String
)

data class CheckPasswordResetResponse(
    val success: Boolean,
    val message: String?,
    val temp_password: String? = null
)

data class AdminPasswordResetRequest(
    val id: Int,
    val user_name: String,
    val role: String,
    val email: String,
    val emp_id: String,
    val status: String,
    val requested_at: String,
    val processed_at: String?,
    val processed_by: Int?
)

data class ApproveResetRequest(
    val request_id: Int
)

data class RejectResetRequest(
    val request_id: Int
)
