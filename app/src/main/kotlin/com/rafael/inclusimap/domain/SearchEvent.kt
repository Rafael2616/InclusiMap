package com.rafael.inclusimap.domain


sealed interface SearchEvent {
    data class OnSearch(val query: String, val allPlaces: List<AccessibleLocalMarker>) : SearchEvent
}