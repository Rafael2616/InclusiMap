package com.rafael.libs.maps.interop.model

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

fun CameraPosition.toMapsCameraPosition() = MapsCameraPosition(
    target = MapsLatLng(target.latitude, target.longitude),
    bearing = bearing,
    tilt = tilt,
    zoom = zoom,
)

fun MapsCameraPosition.toCameraPosition() = CameraPosition(
    LatLng(target.latitude, target.longitude),
    zoom,
    tilt,
    bearing,
)
