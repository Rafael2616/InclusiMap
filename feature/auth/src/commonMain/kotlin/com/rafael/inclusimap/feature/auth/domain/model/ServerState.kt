package com.rafael.inclusimap.feature.auth.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ServerState(
    val isOn: Boolean = true,
)
