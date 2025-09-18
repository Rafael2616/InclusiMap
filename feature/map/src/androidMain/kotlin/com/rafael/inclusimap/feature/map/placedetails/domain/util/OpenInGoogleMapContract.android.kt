package com.rafael.inclusimap.feature.map.placedetails.domain.util

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import com.rafael.libs.maps.interop.model.MapsLatLng
import org.koin.java.KoinJavaComponent.inject

actual fun openInGoogleMap(latLng: MapsLatLng, placeID: String?) {
    val context: Context by inject(Application::class.java)
    val latitude = latLng.latitude
    val longitude = latLng.longitude

    val gmmIntentUri: Uri = if (placeID != null) {
        "https://www.google.com/maps/search/?api=1&query_place_id=$placeID".toUri()
    } else {
        "geo:$latitude,$longitude?z=17".toUri()
    }

    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    mapIntent.putExtra(Intent.EXTRA_REFERRER_NAME, context.packageName)
    mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    try {
        context.startActivity(mapIntent)
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        try {
            val webIntentUri = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude".toUri()
            val webIntent = Intent(Intent.ACTION_VIEW, webIntentUri)
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Nenhum aplicativo de mapas encontrado.", Toast.LENGTH_LONG)
                .show()
        }
    }
}
