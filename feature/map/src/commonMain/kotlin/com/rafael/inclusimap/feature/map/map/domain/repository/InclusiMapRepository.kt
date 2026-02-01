package com.rafael.inclusimap.feature.map.map.domain.repository

import com.rafael.inclusimap.feature.map.map.domain.model.InclusiMapEntity

interface InclusiMapRepository {
    suspend fun getPosition(id: Int): InclusiMapEntity?
    suspend fun updatePosition(position: InclusiMapEntity)
}
