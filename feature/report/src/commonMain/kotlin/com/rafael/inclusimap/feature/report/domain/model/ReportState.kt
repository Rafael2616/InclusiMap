package com.rafael.inclusimap.feature.report.domain.model

data class ReportState(
    val isReported: Boolean = false,
    val isReporting: Boolean = false,
    val isError: Boolean = false,
)
