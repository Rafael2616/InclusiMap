package com.rafael.inclusimap.feature.map.placedetails.domain.util

import com.rafael.libs.maps.interop.model.MapsLatLng
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.UIKit.UIApplication

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun openInGoogleMap(latLng: MapsLatLng, placeID: String?) {
    val googleMapsInstalled = UIApplication.sharedApplication.canOpenURL(NSURL(string = "comgooglemaps://"))
    val latitude = latLng.latitude
    val longitude = latLng.longitude

    val urlString: String = if (googleMapsInstalled) {
        if (placeID != null) {
            val encodedPlaceId = placeID.percentEncoded()
            "comgooglemaps://?q=&center=$latitude,$longitude&zoom=14&views=traffic&q_place_id=$encodedPlaceId"
        } else {
            "comgooglemaps://?q=$latitude,$longitude&center=$latitude,$longitude&zoom=14&views=traffic"
        }
    } else {
        val encodedQuery = "$latitude,$longitude".percentEncoded()
        "https://www.google.com/maps/search/?api=1&query=$encodedQuery"
    }

    val url = NSURL(string = urlString)
    if (UIApplication.sharedApplication.canOpenURL(url)) {
        UIApplication.sharedApplication.openURL(url)
    } else {
        println("Não foi possível abrir a URL do mapa: $urlString")
        val fallbackUrlString = "https://www.google.com/maps/@?api=1&map_action=map&center=$latitude,$longitude&zoom=14"
        val fallbackUrl = NSURL(string = fallbackUrlString)
        fallbackUrl.let { fbUrl ->
            if (UIApplication.sharedApplication.canOpenURL(fbUrl)) {
                UIApplication.sharedApplication.openURL(fbUrl)
            } else {
                println("Não foi possível abrir a URL de fallback do mapa: $fallbackUrlString")
            }
        }
    }
}

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
fun String.percentEncoded(): String = (this as NSString).stringByAddingPercentEncodingWithAllowedCharacters(
    NSCharacterSet.URLQueryAllowedCharacterSet,
) ?: this
