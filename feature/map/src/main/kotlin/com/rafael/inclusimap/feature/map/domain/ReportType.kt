package com.rafael.inclusimap.feature.map.domain

enum class ReportType {
    LOCAL,
    COMMENT,
    IMAGE,
    OTHER
}

fun ReportType.toText() = when (this) {
    ReportType.LOCAL -> "O local"
    ReportType.COMMENT -> "Um comentário"
    ReportType.IMAGE -> "Uma imagem"
    ReportType.OTHER -> "Outro"
}
