package com.rafael.inclusimap.feature.map.search.data.repository

import com.rafael.inclusimap.feature.map.search.data.dao.MapSearchDao
import com.rafael.inclusimap.feature.map.search.domain.model.MapSearchEntity
import com.rafael.inclusimap.feature.map.search.domain.repository.MapSearchRepository

class MapSearchRepositoryImpl(
    private val dao: MapSearchDao
) : MapSearchRepository {
    override suspend fun getHistory(): String {
        return dao.getHistory(1)?.placeHistory ?: "[]"
    }

    override suspend fun updateHistory(history: String) {
        dao.updateHistory(MapSearchEntity(1, history))
    }
}
