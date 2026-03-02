package com.simats.resolveiq_frontend.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val full_name: String?,
    val email: String?,
    val phone: String?,
    val role: String?,
    @SerializedName("require_password_change")
    val requirePasswordChange: Boolean = false,
    val location: String? = null,
    val department_name: String? = null,
    val team_lead_name: String? = null,
    val joining_date: String? = null
)
