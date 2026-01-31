package com.rafael.inclusimap.feature.report.domain.model

import com.rafael.inclusimap.core.util.map.model.AccessibleLocalMarker
import kotlinx.serialization.Serializable

@Serializable
data class Report(
    val type: ReportType,
    val content: String,
    val reportedLocal: AccessibleLocalMarker,
    val userEmail: String? = null,
)
