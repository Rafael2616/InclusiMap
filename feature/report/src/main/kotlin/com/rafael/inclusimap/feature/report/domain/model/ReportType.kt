package com.rafael.inclusimap.feature.report.domain.model

enum class ReportType {
    LOCAL,
    COMMENT,
    IMAGE,
    OTHER,
}

fun ReportType.toText() =
    when (this) {
        ReportType.LOCAL -> "O local"
        ReportType.COMMENT -> "Um comentário"
        ReportType.IMAGE -> "Uma imagem"
        ReportType.OTHER -> "Outro"
    }
