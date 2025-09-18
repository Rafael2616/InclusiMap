package com.rafael.inclusimap.feature.map.search.domain.model

import com.rafael.inclusimap.core.util.map.model.AccessibleLocalMarker

data class SearchState(
    val matchingPlaces: List<AccessibleLocalMarker> = emptyList(),
    val searchQuery: String = "",
    val placesHistory: List<String> = emptyList(),
    val expanded: Boolean = false,
)
