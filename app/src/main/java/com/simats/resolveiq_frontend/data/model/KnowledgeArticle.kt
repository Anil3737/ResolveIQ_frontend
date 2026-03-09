package com.simats.resolveiq_frontend.data.model

import java.io.Serializable

data class KnowledgeArticle(
    val id: Int,
    val title: String,
    val summary: String,
    val content: String,
    val category: String,
    val date: String
) : Serializable
