package com.simats.resolveiq_frontend.data.model

data class UpdateTicketActionRequest(
    val ticket_id: Int,
    val action: String // ACCEPT, DECLINE, RESOLVE
)
