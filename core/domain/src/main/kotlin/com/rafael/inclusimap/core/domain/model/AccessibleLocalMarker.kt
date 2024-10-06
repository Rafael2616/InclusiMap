package com.rafael.inclusimap.core.domain.model

import com.google.api.services.drive.model.File
import kotlinx.serialization.Serializable

// The Place Marker showed in Map Screen
@Serializable
data class AccessibleLocalMarker(
    override var position: Pair<Double, Double> = 0.0 to 0.0,
    override var title: String = "",
    override var category: PlaceCategory? = null,
    override var authorEmail: String = "",
    override var comments: List<Comment> = emptyList(),
    override var time: String = "",
    override var id: String? = null,
) : BaseLocalMarker

interface BaseLocalMarker {
    var position: Pair<Double, Double>
    var title: String
    var category: PlaceCategory?
    var authorEmail: String
    var comments: List<Comment>
    var time: String
    var id: String?
}

data class FullAccessibleLocalMarker(
    override var position: Pair<Double, Double> = 0.0 to 0.0,
    override var title: String = "",
    override var category: PlaceCategory? = null,
    override var authorEmail: String = "",
    override var comments: List<Comment> = emptyList(),
    override var time: String = "",
    val images: List<PlaceImage?> = emptyList(),
    val imageFolder: List<File>? = null,
    val imageFolderId: String? = null,
    override var id: String? = null,
) : BaseLocalMarker

fun AccessibleLocalMarker.toFullAccessibleLocalMarker(
    images: List<PlaceImage?>,
    imageFolder: List<File>?,
    imageFolderId: String?,
): FullAccessibleLocalMarker = FullAccessibleLocalMarker(
    position = position,
    title = title,
    category = category,
    authorEmail = authorEmail,
    comments = comments,
    images = images,
    time = time,
    id = id,
    imageFolder = imageFolder,
    imageFolderId = imageFolderId,
)

fun FullAccessibleLocalMarker.toAccessibleLocalMarker(): AccessibleLocalMarker = AccessibleLocalMarker(
    position = position,
    title = title,
    category = category,
    authorEmail = authorEmail,
    comments = comments,
    time = time,
    id = id,
)
