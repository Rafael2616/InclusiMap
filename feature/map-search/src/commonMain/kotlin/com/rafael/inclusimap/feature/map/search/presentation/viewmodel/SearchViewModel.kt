package com.rafael.inclusimap.feature.map.search.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.inclusimap.core.util.map.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.util.map.model.toCategoryName
import com.rafael.inclusimap.core.util.normalizeText
import com.rafael.inclusimap.core.util.similarity
import com.rafael.inclusimap.feature.map.search.domain.model.SearchEvent
import com.rafael.inclusimap.feature.map.search.domain.model.SearchState
import com.rafael.inclusimap.feature.map.search.domain.repository.MapSearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
            is SearchEvent.RemoveNonexistentPlaceFromHistory -> removeInexistentPlaceFromHistory(event.placeId)
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

        val normalizedQuery = normalizeText(query)
        val exactMatches = defaultSearch(normalizedQuery, allPlaces)
        val fuzzyMatches = lavenshteinSearch(normalizedQuery, allPlaces)
        val combinedResults = (exactMatches + fuzzyMatches).distinctBy { it.id }
        _state.update { it.copy(matchingPlaces = combinedResults) }
    }

    private fun defaultSearch(
        query: String,
        allPlaces: List<AccessibleLocalMarker>,
    ): List<AccessibleLocalMarker> = allPlaces.filter {
        normalizeText(it.title).contains(
            query,
            ignoreCase = true,
        ) ||
            normalizeText(it.category?.toCategoryName() ?: "")
                .contains(query, ignoreCase = true)
    }

    private fun lavenshteinSearch(
        query: String,
        allPlaces: List<AccessibleLocalMarker>,
    ): List<AccessibleLocalMarker> {
        val threshold = 0.7 // 70% of minimum similarity
        return allPlaces.filter { place ->
            val titleSimilarity = similarity(normalizeText(place.title), query)
            val categorySimilarity = place.category?.let { cat ->
                similarity(normalizeText(cat.toCategoryName()), query)
            } ?: 0.0
            titleSimilarity >= threshold || categorySimilarity >= threshold
        }
    }

    private fun loadHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val places = json.decodeFromString<List<String>>(searchRepository.getHistory())
            _state.update { it.copy(placesHistory = places.reversed()) }
        }
    }

    private fun updateHistory(placeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val places =
                json.decodeFromString<List<String>>(searchRepository.getHistory()).toMutableList()
            if (placeId in places) {
                places.remove(placeId)
            }
            places += placeId
            if (places.size > 5) {
                places.removeAt(0)
            }
            searchRepository.updateHistory(json.encodeToString(places))
            _state.update { it.copy(placesHistory = places.reversed()) }
        }
    }

    private fun deleteFromHistory(placeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val places =
                json.decodeFromString<List<String>>(searchRepository.getHistory()).toMutableList()
            places.remove(placeId)
            _state.update { it.copy(placesHistory = places) }
            searchRepository.updateHistory(json.encodeToString(places))
        }
    }

    private fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val places =
                json.decodeFromString<List<String>>(searchRepository.getHistory()).toMutableList()
            places.clear()
            _state.update { it.copy(placesHistory = places) }
            searchRepository.updateHistory(json.encodeToString(places))
        }
    }

    private fun removeInexistentPlaceFromHistory(placeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val places =
                json.decodeFromString<List<String>>(searchRepository.getHistory()).toMutableList()
            places.remove(placeId)
            _state.update { it.copy(placesHistory = places) }
            searchRepository.updateHistory(json.encodeToString(places))
        }
    }
}
