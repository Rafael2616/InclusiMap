package com.rafael.inclusimap.feature.map.search.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.toCategoryName
import com.rafael.inclusimap.feature.map.search.domain.model.SearchEvent
import com.rafael.inclusimap.feature.map.search.domain.model.SearchState
import com.rafael.inclusimap.feature.map.search.domain.repository.MapSearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SearchViewModel(
    private val searchRepository: MapSearchRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state = _state.asStateFlow()
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.OnSearch -> onSearch(event.query, event.allPlaces)
            is SearchEvent.SetExpanded -> _state.update { it.copy(expanded = event.expanded) }
            SearchEvent.LoadHistory -> loadHistory()
            is SearchEvent.UpdateHistory -> updateHistory(event.placeId)
            is SearchEvent.DeleteFromHistory -> deleteFromHistory(event.placeId)
            SearchEvent.ClearHistory -> clearHistory()
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

    private fun loadHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val places = json.decodeFromString<List<String>>(searchRepository.getHistory())
            _state.update { it.copy(placesHistory = places) }
        }
    }
    private fun updateHistory(placeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val places = json.decodeFromString<List<String>>(searchRepository.getHistory()).toMutableList()
            if (placeId in places) {
                val temp = places.indexOf(placeId)
                places.removeAt(temp)
                places.add(0, placeId)
                return@launch
            }
            if (places.size >= 3) {
                places.subList(0, 2)
                places.removeAt(2)
            }
            places.add(0, placeId)
            _state.update { it.copy(placesHistory = places) }
            searchRepository.updateHistory(json.encodeToString(places))
        }
    }

    private fun deleteFromHistory(placeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val places = json.decodeFromString<List<String>>(searchRepository.getHistory()).toMutableList()
            places.remove(placeId)
            _state.update { it.copy(placesHistory = places) }
            searchRepository.updateHistory(json.encodeToString(places))
        }
    }

    private fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val places = json.decodeFromString<List<String>>(searchRepository.getHistory()).toMutableList()
            places.clear()
            _state.update { it.copy(placesHistory = places) }
            searchRepository.updateHistory(json.encodeToString(places))
        }
    }
}
