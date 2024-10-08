package com.rafael.inclusimap.feature.map.domain.repository

import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.feature.map.domain.AccessibleLocalsEntity

interface AccessibleLocalsRepository {
    suspend fun getAccessibleLocals(): List<AccessibleLocalMarker>?
    suspend fun saveAccessibleLocal(accessibleLocal: AccessibleLocalMarker)
    suspend fun updateAccessibleLocal(accessibleLocal: AccessibleLocalMarker)
    suspend fun deleteAccessibleLocal(id: String)

    // ROOM
    suspend fun getAccessibleLocalsStored(id: Int): List<AccessibleLocalMarker>
    suspend fun updateAccessibleLocalStored(accessibleLocalEntity: AccessibleLocalsEntity)
}
