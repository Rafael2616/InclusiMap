package com.rafael.inclusimap.core.domain.network

// @author Philipp Lackner https://github.com/philipplackner

enum class NetworkError : Error {
    REQUEST_TIMEOUT,
    UNAUTHORIZED,
    CONFLICT,
    TOO_MANY_REQUESTS,
    NO_INTERNET,
    PAYLOAD_TOO_LARGE,
    SERVER_ERROR,
    SERIALIZATION,
    UNKNOWN,
}
