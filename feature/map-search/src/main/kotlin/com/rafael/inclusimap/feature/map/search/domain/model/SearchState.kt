package com.rafael.inclusimap.feature.map.search.domain.model

import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker

data class SearchState(
    val matchingPlaces: List<AccessibleLocalMarker> = emptyList(),
    val searchQuery: String = "",
)
