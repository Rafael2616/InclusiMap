package com.rafael.inclusimap.feature.map.search.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.toCategoryName
import com.rafael.inclusimap.feature.map.search.domain.model.SearchEvent
import com.rafael.inclusimap.feature.map.search.domain.model.SearchState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SearchViewModel : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state = _state.asStateFlow()

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.OnSearch -> onSearch(event.query, event.allPlaces)
            is SearchEvent.SetExpanded -> _state.update {
                it.copy(expanded = event.expanded)
            }
        }
    }

    private fun onSearch(query: String, allPlaces: List<AccessibleLocalMarker>) {
        _state.update {
            it.copy(searchQuery = query)
        }
        if (query.isEmpty() || query.isBlank()) {
            _state.update {
                it.copy(matchingPlaces = emptyList())
            }
            return
        }
        allPlaces.filter {
            it.title.contains(query, ignoreCase = true) || it.category?.toCategoryName()?.contains(query, ignoreCase = true) == true
        }.also { places ->
            _state.update {
                it.copy(matchingPlaces = places)
            }
        }
    }
}
