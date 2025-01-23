package com.rafael.inclusimap.feature.auth.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    var name: String,
    val email: String,
    var password: String,
    var showProfilePictureOptedIn: Boolean,
    var isBanned: Boolean,
)
