package com.rafael.inclusimap.feature.map.domain.repository

import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker

interface AccessibleLocalsRepository {
    suspend fun getAccessibleLocals(): List<AccessibleLocalMarker>
    suspend fun saveAccessibleLocal(accessibleLocal: AccessibleLocalMarker)
    suspend fun updateAccessibleLocal(accessibleLocal: AccessibleLocalMarker)
    suspend fun deleteAccessibleLocal(id: String)
}
