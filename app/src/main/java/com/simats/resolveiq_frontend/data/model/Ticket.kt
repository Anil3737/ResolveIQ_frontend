package com.simats.resolveiq_frontend.data.model

import java.io.Serializable

data class Ticket(
    val id: Int,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    val department_id: Int?,
    val created_at: String?
) : Serializable
