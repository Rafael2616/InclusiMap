package com.rafael.libs.maps.interop.model

import cocoapods.GoogleMaps.GMSMarker
import platform.UIKit.UIColor
import platform.UIKit.UIImage

fun MapBitmapDescriptorFactory.toMarkerIcon(): UIImage? = when (this) {
    MapBitmapDescriptorFactory.HUE_YELLOW -> GMSMarker.markerImageWithColor(UIColor.yellowColor)
    MapBitmapDescriptorFactory.HUE_GREEN -> GMSMarker.markerImageWithColor(UIColor.greenColor)
    MapBitmapDescriptorFactory.HUE_RED -> GMSMarker.markerImageWithColor(UIColor.redColor)
}
