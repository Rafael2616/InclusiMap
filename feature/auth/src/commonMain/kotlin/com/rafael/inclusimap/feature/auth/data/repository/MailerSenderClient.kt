package com.rafael.inclusimap.feature.auth.data.repository

import com.rafael.inclusimap.feature.auth.domain.model.From
import com.rafael.inclusimap.feature.auth.domain.model.MailerSendEmailRequest
import com.rafael.inclusimap.feature.auth.domain.model.To
import com.rafael.inclusimap.feature.auth.domain.repository.MailerSenderProperties
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType

class MailerSenderClient(
    private val client: HttpClient,
) {
    private val properties = MailerSenderProperties()
    private val apiKey = properties.getApiKey()
    private val sender = properties.getDomain()

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
                println("E-mail sent successfully!")
            } else {
                println("Failed to send email with status code: ${response.status}, ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            client.close()
        }
    }
}
