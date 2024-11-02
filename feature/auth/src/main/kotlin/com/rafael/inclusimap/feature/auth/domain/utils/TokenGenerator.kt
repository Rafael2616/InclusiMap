package com.rafael.inclusimap.feature.auth.domain.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

fun generateToken(byteLength: Int = 8): String {
    val random = SecureRandom()
    val bytes = ByteArray(byteLength)
    random.nextBytes(bytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}

fun hashToken(token: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(token.toByteArray(Charsets.UTF_8))
    return Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
}

fun verifyToken(receivedToken: String, storedHashToken: String, expirationTimeMillis: Long): Boolean {
    if (System.currentTimeMillis() > expirationTimeMillis) {
        return false
    }

    val receivedHash = hashToken(receivedToken)
    return receivedHash == storedHashToken
}
