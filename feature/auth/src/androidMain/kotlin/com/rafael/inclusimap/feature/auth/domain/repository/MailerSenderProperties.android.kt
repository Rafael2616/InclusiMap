package com.rafael.inclusimap.feature.auth.domain.repository

import java.util.Properties
import kotlin.use
import kotlinx.io.files.FileNotFoundException

actual class MailerSenderProperties {
    actual fun getApiKey(): String? {
        val properties = loadMailSenderProperties()
        return properties.getProperty("MAILERSEND_API_KEY")
    }

    actual fun getDomain(): String? {
        val properties = loadMailSenderProperties()
        return properties.getProperty("INCLUSIMAP_DOMAIN")
    }

    private fun loadMailSenderProperties(): Properties {
        val credentialsStream =
            this::class.java.getResourceAsStream("/mailsender_credentials.properties")
                ?: throw FileNotFoundException("Resource not found: mailsender_credentials.properties")

        val properties = Properties()
        credentialsStream.use { inputStream ->
            properties.load(inputStream)
        }
        return properties
    }
}
