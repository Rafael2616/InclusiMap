package com.rafael.inclusimap.core.util

import java.text.Normalizer

actual fun normalizeText(text: String): String = Normalizer
    .normalize(text, Normalizer.Form.NFD)
    .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
