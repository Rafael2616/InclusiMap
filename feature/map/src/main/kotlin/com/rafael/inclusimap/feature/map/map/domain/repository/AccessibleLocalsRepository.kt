package com.rafael.inclusimap.feature.map.map.domain.repository

import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.feature.map.map.domain.AccessibleLocalsEntity

interface AccessibleLocalsRepository {
    suspend fun getAccessibleLocals(): List<AccessibleLocalMarker>?

    suspend fun saveAccessibleLocal(accessibleLocal: AccessibleLocalMarker): String?

    suspend fun updateAccessibleLocal(accessibleLocal: AccessibleLocalMarker)

    suspend fun deleteAccessibleLocal(id: String)

    // ROOM
    suspend fun getAccessibleLocalsStored(id: Int): AccessibleLocalsEntity?

    suspend fun updateAccessibleLocalStored(accessibleLocalEntity: AccessibleLocalsEntity)
}
