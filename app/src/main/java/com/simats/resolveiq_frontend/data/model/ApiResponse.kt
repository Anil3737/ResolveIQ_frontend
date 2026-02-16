package com.simats.resolveiq_frontend.data.model

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)
