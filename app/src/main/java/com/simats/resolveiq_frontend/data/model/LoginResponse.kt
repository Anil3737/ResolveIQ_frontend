package com.simats.resolveiq_frontend.data.model

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val data: LoginData?
)

data class LoginData(
    val access_token: String,
    val user: User
)
