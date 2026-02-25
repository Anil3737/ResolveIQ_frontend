package com.simats.resolveiq_frontend.data.model

data class Employee(
    val employeeId: String,
    val fullName: String,
    val role: String? = null,
    val department: String? = null,
    val email: String? = null,
    val location: String? = null,
    val teamLead: String? = null,
    val joiningDate: String? = null
)
