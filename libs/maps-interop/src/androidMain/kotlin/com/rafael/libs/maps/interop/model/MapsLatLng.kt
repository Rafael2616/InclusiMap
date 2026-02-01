package com.rafael.libs.maps.interop.model

import com.google.android.gms.maps.model.LatLng

fun MapsLatLng.toLatLng() = LatLng(latitude, longitude)
