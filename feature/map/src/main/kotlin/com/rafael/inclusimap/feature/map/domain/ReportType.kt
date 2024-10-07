package com.rafael.inclusimap.feature.map.domain

enum class ReportType {
    LOCAL,
    COMMENT,
    IMAGE,
    OTHER
}

fun ReportType.toText() = when (this) {
    ReportType.LOCAL -> "Local"
    ReportType.COMMENT -> "Comentário"
    ReportType.IMAGE -> "Imagem"
    ReportType.OTHER -> "Outro"
}
