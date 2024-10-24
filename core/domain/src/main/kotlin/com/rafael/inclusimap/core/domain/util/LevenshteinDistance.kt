package com.rafael.inclusimap.core.domain.util

import java.text.Normalizer

fun levenshteinDistance(a: String, b: String): Int {
    val matrix = Array(b.length + 1) { IntArray(a.length + 1) }

    for (i in 0..a.length) matrix[0][i] = i
    for (j in 0..b.length) matrix[j][0] = j

    for (i in 1..a.length) {
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            matrix[j][i] = minOf(
                matrix[j - 1][i] + 1,
                matrix[j][i - 1] + 1,
                matrix[j - 1][i - 1] + cost,
            )
        }
    }

    return matrix[b.length][a.length]
}

fun similarity(a: String, b: String): Double {
    val maxLength = maxOf(a.length, b.length)
    val distance = levenshteinDistance(a.lowercase(), b.lowercase())
    return 1.0 - (distance.toDouble() / maxLength)
}

fun normalizeText(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFD)
    .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
