package com.rafael.inclusimap.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val postDate: String,
    val id: Int,
    val name: String,
    val email: String,
    val body: String,
    val accessibilityRate: Int,
)
