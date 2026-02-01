package com.rafael.inclusimap.feature.auth.domain.utils

import io.ktor.utils.io.core.toByteArray
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalEncodingApi::class)
fun generateToken(byteLength: Int = 8): String {
    var bytes = ByteArray(byteLength)
    bytes = Random.nextBytes(bytes)
    return Base64.encodeToByteArray(bytes).decodeToString()
}

@OptIn(ExperimentalEncodingApi::class)
fun hashToken(token: String): String {
    val hash = token.hashCode().toString()
    return Base64.encodeToByteArray(hash.toByteArray()).decodeToString()
}

@OptIn(ExperimentalTime::class)
fun verifyToken(receivedToken: String, storedHashToken: String, expirationTimeMillis: Long): Boolean {
    if (System.now().toEpochMilliseconds() > expirationTimeMillis) {
        return false
    }

    val receivedHash = hashToken(receivedToken)
    return receivedHash == storedHashToken
}
