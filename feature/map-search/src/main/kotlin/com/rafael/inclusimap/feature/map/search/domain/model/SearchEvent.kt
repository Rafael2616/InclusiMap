package com.rafael.inclusimap.feature.map.search.domain.model

import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker

sealed interface SearchEvent {
    data class OnSearch(val query: String, val allPlaces: List<AccessibleLocalMarker>) : SearchEvent
    data class SetExpanded(val expanded: Boolean) : SearchEvent
    data class UpdateHistory(val placeId: String) : SearchEvent
    data class DeleteFromHistory(val placeId: String) : SearchEvent
    data object ClearHistory : SearchEvent
    data object LoadHistory : SearchEvent
    data class RemoveNonexistentPlaceFromHistory(val placeId: String) : SearchEvent
}
