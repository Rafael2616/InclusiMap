package com.rafael.inclusimap.domain.repository

import com.google.android.gms.maps.model.LatLng
import com.rafael.inclusimap.domain.AccessibleLocalMarker

interface AccessibleLocalsRepository {
    fun getAccessibleLocals(): List<AccessibleLocalMarker>
    fun saveAccessibleLocal(accessibleLocal: AccessibleLocalMarker)
    fun deleteAccessibleLocal(id: LatLng)
}