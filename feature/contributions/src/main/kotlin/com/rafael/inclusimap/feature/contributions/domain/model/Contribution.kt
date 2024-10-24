package com.rafael.inclusimap.feature.contributions.domain.model

import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.Comment
import com.rafael.inclusimap.core.domain.model.PlaceImage
import kotlinx.serialization.Serializable

data class Contributions(
    val images: List<PlaceImageWithPlace> = emptyList(),
    val comments: List<CommentWithPlace> = emptyList(),
    val places: List<AccessibleLocalMarkerWithFileId> = emptyList(),
    val resources: List<AccessibleLocalMarkerWithFileId> = emptyList(),
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
    ACCESSIBLE_RESOURCES
}

data class PlaceImageWithPlace(
    val placeImage: PlaceImage,
    val place: AccessibleLocalMarker,
    val date: String?,
    val fileId: String,
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
