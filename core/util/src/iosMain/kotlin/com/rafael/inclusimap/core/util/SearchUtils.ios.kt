package com.rafael.inclusimap.core.util

actual fun normalizeText(text: String): String {
    return text // Kotlin/Native doesn't support Unicode normalization
}
