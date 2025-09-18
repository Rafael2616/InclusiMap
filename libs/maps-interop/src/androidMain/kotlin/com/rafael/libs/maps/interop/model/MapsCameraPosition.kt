package com.rafael.libs.maps.interop.model

import com.google.android.gms.maps.model.CameraPosition

fun CameraPosition.toMapsCameraPosition() = MapsCameraPosition(
    target = MapsLatLng(target.latitude, target.longitude),
    bearing = bearing,
    tilt = tilt,
    zoom = zoom,
)

fun MapsCameraPosition.toCameraPosition() = CameraPosition(
    MapsLatLng(target.latitude, target.longitude).toLatLng(),
    zoom,
    tilt,
    bearing,
)
