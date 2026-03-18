package com.simats.resolveiq_frontend.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Int,
    @SerializedName("full_name")
    val full_name: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("role")
    val role: String?,
    @SerializedName("require_password_change")
    val requirePasswordChange: Boolean = false,
    @SerializedName("location")
    val location: String? = null,
    @SerializedName("department_name")
    val department_name: String? = null,
    @SerializedName("team_lead_name")
    val team_lead_name: String? = null,
    @SerializedName("joining_date")
    val joining_date: String? = null
)
