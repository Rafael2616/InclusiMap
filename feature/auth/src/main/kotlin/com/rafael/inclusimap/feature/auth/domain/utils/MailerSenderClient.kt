package com.rafael.inclusimap.feature.auth.domain.utils

import com.rafael.inclusimap.feature.auth.domain.model.From
import com.rafael.inclusimap.feature.auth.domain.model.MailerSendEmailRequest
import com.rafael.inclusimap.feature.auth.domain.model.To
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.io.FileNotFoundException
import java.util.Properties

class MailerSenderClient(
    private val client: HttpClient,
) {
    private val apiKey = getApiKey()
    private val sender = getDomain()

    suspend fun sendEmail(
        receiver: String,
        subject: String,
        html: String?,
    ) {
        val from = sender?.let { From(it) }
        val request = from?.let {
            MailerSendEmailRequest(
                from = it,
                to = listOf(To(receiver)),
                subject = subject,
                html = html ?: "",
            )
        }

        try {
            val response = client.post("https://api.mailersend.com/v1/email") {
                header("Authorization", "Bearer $apiKey")
                header("X-Requested-With", "XMLHttpRequest")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.value == 202) {
                println("E-mail sent sucessfully!")
            } else {
                println("Failed to send email with status code: ${response.status}, ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            client.close()
        }
    }

    private fun loadMailSenderProperties(): Properties {
        val credentialsStream =
            this.javaClass.getResourceAsStream("/mailsender_credentials.properties")
                ?: throw FileNotFoundException("Resource not found: mailsender_credentials.properties")

        val properties = Properties()
        credentialsStream.use { inputStream ->
            properties.load(inputStream)
        }
        return properties
    }

    private fun getApiKey(): String? {
        val properties = loadMailSenderProperties()
        return properties.getProperty("MAILERSEND_API_KEY")
    }

    private fun getDomain(): String? {
        val properties = loadMailSenderProperties()
        return properties.getProperty("INCLUSIMAP_DOMAIN")
    }
}
