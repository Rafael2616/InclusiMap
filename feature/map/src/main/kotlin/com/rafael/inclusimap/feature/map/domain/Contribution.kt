package com.rafael.inclusimap.feature.map.domain

import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.Comment
import com.rafael.inclusimap.core.domain.model.PlaceImage
import kotlinx.serialization.Serializable

data class Contributions(
    val images: List<PlaceImageWithPlace> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val places: List<AccessibleLocalMarker> = emptyList(),
)

@Serializable
data class Contribution(
    val type: ContributionType,
    val fileId: String,
)

enum class ContributionType {
    IMAGE,
    COMMENT,
    PLACE,
}

data class PlaceImageWithPlace(
    val placeImage: PlaceImage,
    val place: AccessibleLocalMarker,
)
