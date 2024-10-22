package com.rafael.inclusimap.feature.report.domain.model

import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.feature.auth.domain.model.User
import kotlinx.serialization.Serializable

@Serializable
data class Report(
    val type: ReportType,
    val content: String,
    val reportedLocal: AccessibleLocalMarker,
    val user: User? = null,
)
