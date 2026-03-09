package com.simats.resolveiq_frontend.data.model

import java.io.Serializable

data class SlaRule(
    val id: Int,
    val department_id: Int,
    val department_name: String?,
    val priority: String,
    val sla_hours: Int
) : Serializable

data class SlaRuleRequest(
    val department_id: Int,
    val priority: String,
    val sla_hours: Int
)

data class SlaRuleUpdateRequest(
    val sla_hours: Int
)

data class SlaRulesResponse(
    val success: Boolean,
    val data: List<SlaRule>?,
    val message: String? = null
)

data class SlaRuleResponse(
    val success: Boolean,
    val data: SlaRule?,
    val message: String? = null
)
