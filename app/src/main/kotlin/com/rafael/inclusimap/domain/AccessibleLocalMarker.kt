package com.rafael.inclusimap.domain

import androidx.compose.ui.graphics.ImageBitmap
import com.google.maps.android.compose.MarkerState
import java.util.Date

// The Place Marker showed in Map Screen
data class AccessibleLocalMarker(
    override var markerState: MarkerState = MarkerState(),
    override var title: String = "",
    override var category: String = "",
    override var author: String = "",
    override var comments: List<Comment> = emptyList(),
    override var time: String = "",
    override var id: String = "",
) : BaseLocalMarker

interface BaseLocalMarker {
    val markerState: MarkerState
    var title: String
    var category: String
    var author: String
    var comments: List<Comment>
    var time: String
    var id: String
}

data class FullAccessibleLocalMarker(
    override var markerState: MarkerState = MarkerState(),
    override var title: String,
    override var category: String,
    override var author: String,
    override var comments: List<Comment>,
    override var time: String,
    val images: List<PlaceImage?>,
    override var id: String,
) : BaseLocalMarker

fun AccessibleLocalMarker.toFullAccessibleLocalMarker(images: List<PlaceImage?>): FullAccessibleLocalMarker = FullAccessibleLocalMarker(
    markerState = markerState,
    title = title,
    category = category,
    author = author,
    comments = comments,
    images = images,
    time = time,
    id = id
)

fun FullAccessibleLocalMarker.toAccessibleLocalMarker(): AccessibleLocalMarker = AccessibleLocalMarker(
    markerState = markerState,
    title = title,
    category = category,
    author = author,
    comments = comments,
    time = time,
    id = id,
)