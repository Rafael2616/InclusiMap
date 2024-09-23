package com.rafael.inclusimap.domain

import com.google.maps.android.compose.MarkerState
import java.util.Date

data class AccessibleLocalMarker(
    var markerState: MarkerState = MarkerState(),
    var title: String = "",
    var description: String = "",
    var author: String = "",
    var comments: List<Comment>? = null,
    var time: String = Date().toInstant().toString(),
)
