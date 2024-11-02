package com.rafael.inclusimap.feature.auth.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MailerSendEmailRequest(
    val from: From,
    val to: List<To>,
    val subject: String,
    val html: String? = null,
)

@Serializable
data class From(val email: String)

@Serializable
data class To(val email: String)
