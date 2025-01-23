package com.rafael.inclusimap.core.services

import android.content.Context
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import java.io.FileNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PlacesApiService(
    context: Context,
) {
    private val placesClient: PlacesClient

    init {
        val credentialsStream =
            this.javaClass.getResourceAsStream("/places_credentials.txt")
                ?: throw FileNotFoundException("Resource not found: places_credentials.txt")
        Places.initializeWithNewPlacesApiEnabled(
            context,
            credentialsStream.bufferedReader().use { it.readText() }.trim(),
        )
        placesClient =
            Places
                .createClient(context)
    }

    suspend fun getNearestPlaceUri(latLng: LatLng): Uri? = try {
        withContext(Dispatchers.IO) {
            val request =
                SearchNearbyRequest.newInstance(
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
