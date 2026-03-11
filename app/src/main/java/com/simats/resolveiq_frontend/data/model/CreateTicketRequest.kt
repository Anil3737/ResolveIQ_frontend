package com.simats.resolveiq_frontend.data.model

import com.google.gson.annotations.SerializedName

data class CreateTicketRequest(
    val title: String,
    val description: String,

    @SerializedName("department_id")
    val departmentId: Int,

    @SerializedName("issue_type")
    val issueType: String,

    @SerializedName("expected_resolution_time")
    val expectedResolutionTime: String?,

    // Unique key per submission attempt — backend can use this to deduplicate
    @SerializedName("idempotency_key")
    val idempotencyKey: String? = null
)

