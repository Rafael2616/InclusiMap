package com.rafael.inclusimap.feature.map.placedetails.domain.util

import com.rafael.libs.maps.interop.model.MapsLatLng

expect fun openInGoogleMap(latLng: MapsLatLng, placeID: String?)
