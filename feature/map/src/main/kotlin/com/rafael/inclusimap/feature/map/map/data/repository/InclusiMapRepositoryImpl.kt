package com.rafael.inclusimap.feature.map.map.data.repository

import com.rafael.inclusimap.feature.map.map.data.dao.InclusiMapDao
import com.rafael.inclusimap.feature.map.map.domain.InclusiMapEntity
import com.rafael.inclusimap.feature.map.map.domain.repository.InclusiMapRepository

class InclusiMapRepositoryImpl(
    private val inclusiMapDao: InclusiMapDao,
) : InclusiMapRepository {
    override suspend fun getPosition(id: Int): InclusiMapEntity? = inclusiMapDao.getPosition(id)

    override suspend fun updatePosition(position: InclusiMapEntity) {
        inclusiMapDao.updatePosition(position)
    }
}
