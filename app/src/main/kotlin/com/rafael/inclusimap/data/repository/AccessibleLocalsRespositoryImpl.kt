package com.rafael.inclusimap.data.repository

import com.google.android.gms.maps.model.LatLng
import com.rafael.inclusimap.domain.AccessibleLocalMarker
import com.rafael.inclusimap.domain.repository.AccessibleLocalsRepository

class AccessibleLocalsRepositoryImpl : AccessibleLocalsRepository {
    override fun getAccessibleLocals(): List<AccessibleLocalMarker> {
        return mappedPlaces
    }
    override fun saveAccessibleLocal(accessibleLocal: AccessibleLocalMarker) {

    }
    override fun deleteAccessibleLocal(id: LatLng) {

    }
}