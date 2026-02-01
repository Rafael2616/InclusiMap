package com.rafael.libs.maps.interop.model

fun MapBitmapDescriptorFactory.toBitmapDescriptorFactory() = when (this) {
    MapBitmapDescriptorFactory.HUE_YELLOW -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_YELLOW
    MapBitmapDescriptorFactory.HUE_GREEN -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN
    MapBitmapDescriptorFactory.HUE_RED -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
}
