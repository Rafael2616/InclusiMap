package com.rafael.inclusimap.feature.map.map.domain

import android.content.Context
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import java.io.FileNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

suspend fun getNearestPlaceId(latLng: LatLng, context: Context): Uri? {
    return try {
        withContext(Dispatchers.IO) {
            val credentialsStream = this.javaClass.getResourceAsStream("/places_credentials.txt")
                ?: throw FileNotFoundException("Resource not found: places_credentials.txt")
            Places.initializeWithNewPlacesApiEnabled(context, credentialsStream.bufferedReader().use { it.readText() }.trim())
            val placesClient = Places
                .createClient(context)
            val request = SearchNearbyRequest.newInstance(
                CircularBounds.newInstance(latLng, 20.0),
                listOf(Field.ID, Field.GOOGLE_MAPS_URI),
            )
            val response = placesClient.searchNearby(request).await()
            response.places.firstOrNull()?.googleMapsUri
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
