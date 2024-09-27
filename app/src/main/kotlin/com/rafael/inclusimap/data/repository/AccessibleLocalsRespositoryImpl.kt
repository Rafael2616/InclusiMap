package com.rafael.inclusimap.data.repository

import com.google.android.gms.maps.model.LatLng
import com.rafael.inclusimap.data.GoogleDriveService
import com.rafael.inclusimap.domain.AccessibleLocalMarker
import com.rafael.inclusimap.domain.repository.AccessibleLocalsRepository
import kotlinx.serialization.json.Json

class AccessibleLocalsRepositoryImpl(
    private val driveService: GoogleDriveService,
) : AccessibleLocalsRepository {
    override suspend fun getAccessibleLocals(): List<AccessibleLocalMarker> {
        return driveService.listFiles("1jmwzV6NJMwkHruu3nYzrZ8i3OLfMT7_0").find {
            it.name == "places.json"
        }.let { file ->
            if (file != null) {
                val json = Json { ignoreUnknownKeys = true }
                val content = driveService.getFileContent(file.id).decodeToString()
                json.decodeFromString<List<AccessibleLocalMarker>>(content)
            } else {
                emptyList()
            }
        }

    }

    override suspend fun saveAccessibleLocal(accessibleLocal: AccessibleLocalMarker) {

    }

    override suspend fun deleteAccessibleLocal(id: LatLng) {

    }
}