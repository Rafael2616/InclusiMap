package com.rafael.inclusimap.feature.auth.domain.repository

expect class MailerSenderProperties() {
    fun getApiKey(): String?

    fun getDomain(): String?
}
