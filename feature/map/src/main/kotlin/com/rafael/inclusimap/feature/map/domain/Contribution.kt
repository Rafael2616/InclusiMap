package com.rafael.inclusimap.feature.map.domain

import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.Comment
import com.rafael.inclusimap.core.domain.model.PlaceImage
import kotlinx.serialization.Serializable

data class Contributions(
    val images: List<PlaceImageWithPlace> = emptyList(),
    val comments: List<CommentWithPlace> = emptyList(),
    val places: List<AccessibleLocalMarkerWithFileId> = emptyList(),
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
    val date: String?,
)

data class CommentWithPlace(
    val comment: Comment,
    val place: AccessibleLocalMarker,
    val fileId: String,
)

data class AccessibleLocalMarkerWithFileId(
    val place: AccessibleLocalMarker,
    val fileId: String,
)
