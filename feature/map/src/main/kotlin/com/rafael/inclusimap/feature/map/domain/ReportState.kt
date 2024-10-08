package com.rafael.inclusimap.feature.map.domain

data class ReportState(
    var isReported: Boolean = false,
    var isReporting: Boolean = false,
    var isError: Boolean = false,
)
