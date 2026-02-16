package com.simats.resolveiq_frontend.data.model

import com.google.gson.annotations.SerializedName

data class CreateTicketRequest(
    val title: String,
    val description: String,

    @SerializedName("department_id")
    val departmentId: Int
)

