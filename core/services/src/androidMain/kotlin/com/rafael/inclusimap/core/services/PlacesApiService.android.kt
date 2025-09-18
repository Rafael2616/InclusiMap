package com.rafael.inclusimap.core.services

import android.app.Application
import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.rafael.libs.maps.interop.model.MapsLatLng
import com.rafael.libs.maps.interop.model.toLatLng
import java.io.FileNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject

actual class PlacesApiService {
    private val placesClient: PlacesClient
    private val context: Context by inject(Application::class.java)
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

    actual suspend fun getNearestPlaceUri(latLng: MapsLatLng): String? = try {
        withContext(Dispatchers.IO) {
            val request =
                SearchNearbyRequest.newInstance(
                    CircularBounds.newInstance(latLng.toLatLng(), 20.0),
                    listOf(Field.ID),
                )
            val response = placesClient.searchNearby(request).await()
            response.places.firstOrNull()?.googleMapsUri.toString()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    actual suspend fun getNearestPlaceId(latLng: MapsLatLng): String? = try {
        withContext(Dispatchers.IO) {
            val request =
                SearchNearbyRequest.newInstance(
                    CircularBounds.newInstance(latLng.toLatLng(), 20.0),
                    listOf(Field.ID),
                )
            val response = placesClient.searchNearby(request).await()
            response.places.firstOrNull()?.id
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
