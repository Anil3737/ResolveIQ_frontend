package com.simats.resolveiq_frontend.data.model

data class ProgressStep(
    val status: Boolean,
    val timestamp: String?
)

data class TicketProgress(
    val created: ProgressStep,
    val approved: ProgressStep,
    val assigned: ProgressStep,
    val accepted: ProgressStep,
    val resolved: ProgressStep,
    val closed: ProgressStep
)

data class TicketDetailResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Ticket,
    val progress: TicketProgress
)
