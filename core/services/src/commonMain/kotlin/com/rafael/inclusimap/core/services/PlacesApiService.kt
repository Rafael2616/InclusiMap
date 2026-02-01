package com.rafael.inclusimap.core.services

import com.rafael.libs.maps.interop.model.MapsLatLng

expect class PlacesApiService() {
    suspend fun getNearestPlaceUri(latLng: MapsLatLng): String?

    suspend fun getNearestPlaceId(latLng: MapsLatLng): String?
}
