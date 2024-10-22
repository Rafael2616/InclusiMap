package com.rafael.inclusimap.feature.report.domain.model

data class ReportState(
    var isReported: Boolean = false,
    var isReporting: Boolean = false,
    var isError: Boolean = false,
)
