package com.rafael.inclusimap.feature.contributions.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class OssLibrary(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val name: String = "unknown",
    val spdxLicenses: List<License>? = null,
    val unknownLicenses: List<License>? = null,
) {
    @Serializable
    data class License(
        val url: String,
    )
}
