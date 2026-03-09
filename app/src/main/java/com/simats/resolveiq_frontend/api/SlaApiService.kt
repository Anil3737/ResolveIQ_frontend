package com.simats.resolveiq_frontend.api

import com.simats.resolveiq_frontend.data.model.SlaRulesResponse
import com.simats.resolveiq_frontend.data.model.SlaRuleResponse
import com.simats.resolveiq_frontend.data.model.SlaRuleRequest
import com.simats.resolveiq_frontend.data.model.SlaRuleUpdateRequest
import retrofit2.Response
import retrofit2.http.*

interface SlaApiService {

    @GET("api/sla/rules")
    suspend fun getRules(): Response<SlaRulesResponse>

    @POST("api/sla/rules")
    suspend fun createRule(@Body request: SlaRuleRequest): Response<SlaRuleResponse>

    @PUT("api/sla/rules/{id}")
    suspend fun updateRule(
        @Path("id") id: Int,
        @Body request: SlaRuleUpdateRequest
    ): Response<SlaRuleResponse>

    @DELETE("api/sla/rules/{id}")
    suspend fun deleteRule(@Path("id") id: Int): Response<SlaRuleResponse>
}
