package com.rafael.inclusimap.domain

import androidx.compose.ui.graphics.ImageBitmap
import com.google.maps.android.compose.MarkerState
import java.util.Date

// The Place Marker showed in Map Screen
data class AccessibleLocalMarker(
    override var markerState: MarkerState = MarkerState(),
    override var title: String = "",
    override var description: String = "",
    override var author: String = "",
    override var comments: List<Comment>? = null,
    override var time: String = Date().toInstant().toString(),
) : BaseLocalMarker

interface BaseLocalMarker {
    val markerState: MarkerState
    var title: String
    var description: String
    var author: String
    var comments: List<Comment>?
    var time: String
}

data class FullAccessibleLocalMarker(
    override var markerState: MarkerState = MarkerState(),
    override var title: String = "",
    override var description: String = "",
    override var author: String = "",
    override var comments: List<Comment>? = null,
    override var time: String = Date().toInstant().toString(),
    val images: List<PlaceImage>? = null
) : BaseLocalMarker


fun FullAccessibleLocalMarker.toAccessibleLocalMarker(): AccessibleLocalMarker = AccessibleLocalMarker(
    markerState = this.markerState,
    title = this.title,
    description = this.description,
    author = this.author,
    comments = this.comments
)

fun AccessibleLocalMarker.toFullAccessibleLocalMarker(images: List<PlaceImage>?): FullAccessibleLocalMarker = FullAccessibleLocalMarker(
    markerState = this.markerState,
    title = this.title,
    description = this.description,
    author = this.author,
    comments = this.comments,
    images = images
)