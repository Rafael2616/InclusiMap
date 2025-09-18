package com.rafael.inclusimap.core.services

import com.rafael.libs.maps.interop.model.MapsLatLng

actual class PlacesApiService {
    private val placesClient: Any = ""

    actual suspend fun getNearestPlaceUri(latLng: MapsLatLng): String? = ""
}
