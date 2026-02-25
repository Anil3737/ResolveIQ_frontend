package com.simats.resolveiq_frontend.data.model

data class ActivityLogResponse(
    val success: Boolean,
    val total: Int,
    val page: Int,
    val pages: Int,
    val limit: Int,
    val logs: List<AdminActivityLog>,
    val message: String? = null
)
