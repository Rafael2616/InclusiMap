package com.rafael.inclusimap.domain

data class SearchState(
    val matchingPlaces: List<AccessibleLocalMarker> = emptyList(),
    val searchQuery: String = ""
)