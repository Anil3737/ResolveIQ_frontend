package com.simats.resolveiq_frontend.data.model

import java.io.Serializable

data class FeedbackRequest(
    val rating: Int,
    val comments: String? = null,
    val suggestions: List<String>? = null
) : Serializable

data class FeedbackResponse(
    val success: Boolean,
    val message: String?,
    val data: FeedbackData?
) : Serializable

data class FeedbackData(
    val id: Int,
    val ticket_id: Int,
    val user_id: Int,
    val rating: Int,
    val comments: String?,
    val suggestions: List<String>?,
    val created_at: String?
) : Serializable
