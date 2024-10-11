package com.rafael.inclusimap.feature.map.domain.repository

import com.rafael.inclusimap.feature.map.domain.InclusiMapEntity

interface InclusiMapRepository {
    suspend fun getPosition(id: Int): InclusiMapEntity?
    suspend fun updatePosition(position: InclusiMapEntity)
}
