package com.simats.resolveiq_frontend.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: LoginData?
)

data class LoginData(
    @SerializedName("access_token")
    val access_token: String,
    @SerializedName("user")
    val user: User
)
