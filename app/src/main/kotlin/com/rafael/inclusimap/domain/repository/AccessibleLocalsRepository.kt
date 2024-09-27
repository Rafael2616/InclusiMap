package com.rafael.inclusimap.domain.repository

import com.google.android.gms.maps.model.LatLng
import com.rafael.inclusimap.domain.AccessibleLocalMarker

interface AccessibleLocalsRepository {
    suspend fun getAccessibleLocals(): List<AccessibleLocalMarker>
    suspend fun saveAccessibleLocal(accessibleLocal: AccessibleLocalMarker)
    suspend fun deleteAccessibleLocal(id: LatLng)
}
