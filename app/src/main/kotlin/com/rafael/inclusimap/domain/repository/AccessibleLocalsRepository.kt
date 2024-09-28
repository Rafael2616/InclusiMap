package com.rafael.inclusimap.domain.repository

import com.rafael.inclusimap.domain.AccessibleLocalMarker

interface AccessibleLocalsRepository {
    suspend fun getAccessibleLocals(): List<AccessibleLocalMarker>
    suspend fun saveAccessibleLocal(accessibleLocal: AccessibleLocalMarker)
    suspend fun updateAccessibleLocal(accessibleLocal: AccessibleLocalMarker)
    suspend fun deleteAccessibleLocal(id: String)
}
