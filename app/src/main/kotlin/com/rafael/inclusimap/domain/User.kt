package com.rafael.inclusimap.domain

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    var password: String,
)