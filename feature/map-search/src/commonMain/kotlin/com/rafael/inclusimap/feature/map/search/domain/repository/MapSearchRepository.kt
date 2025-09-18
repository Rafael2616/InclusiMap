package com.rafael.inclusimap.feature.map.search.domain.repository

interface MapSearchRepository {
    suspend fun getHistory(): String

    suspend fun updateHistory(history: String)
}
