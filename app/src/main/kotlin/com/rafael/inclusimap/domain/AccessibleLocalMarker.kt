package com.rafael.inclusimap.domain

import kotlinx.serialization.Serializable

// The Place Marker showed in Map Screen
@Serializable
data class AccessibleLocalMarker(
    override var position: Pair<Double, Double> = 0.0 to 0.0,
    override var title: String = "",
    override var category: String = "",
    override var authorEmail: String = "",
    override var comments: List<Comment> = emptyList(),
    override var time: String = "",
    override var id: String = "",
) : BaseLocalMarker

interface BaseLocalMarker {
    var position: Pair<Double, Double>
    var title: String
    var category: String
    var authorEmail: String
    var comments: List<Comment>
    var time: String
    var id: String
}

data class FullAccessibleLocalMarker(
    override var position: Pair<Double, Double>,
    override var title: String,
    override var category: String,
    override var authorEmail: String,
    override var comments: List<Comment>,
    override var time: String,
    val images: List<PlaceImage?>,
    override var id: String,
) : BaseLocalMarker

fun AccessibleLocalMarker.toFullAccessibleLocalMarker(images: List<PlaceImage?>): FullAccessibleLocalMarker = FullAccessibleLocalMarker(
    position = position,
    title = title,
    category = category,
    authorEmail = authorEmail,
    comments = comments,
    images = images,
    time = time,
    id = id
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