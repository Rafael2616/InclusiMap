package com.rafael.inclusimap.data.repository

import com.rafael.inclusimap.data.GoogleDriveService
import com.rafael.inclusimap.domain.AccessibleLocalMarker
import com.rafael.inclusimap.domain.repository.AccessibleLocalsRepository
import com.rafael.inclusimap.domain.util.Constants.INCLUSIMAP_PLACE_DATA_FOLDER_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AccessibleLocalsRepositoryImpl(
    private val driveService: GoogleDriveService,
) : AccessibleLocalsRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    override suspend fun getAccessibleLocals(): List<AccessibleLocalMarker> {
        return withContext(Dispatchers.IO) {
            async {
                driveService.listFiles(INCLUSIMAP_PLACE_DATA_FOLDER_ID).find {
                    it.name == "places.json"
                }
            }.await().let { file ->
                if (file == null) {
                    return@withContext emptyList()
                }
                val content = driveService.getFileContent(file.id).decodeToString()
                json.decodeFromString<List<AccessibleLocalMarker>>(content)
            }
        }
    }

    override suspend fun saveAccessibleLocal(accessibleLocal: AccessibleLocalMarker) {
        withContext(Dispatchers.IO) {
            val places = getAccessibleLocals().toMutableList()
            places.add(accessibleLocal)
            val updatedPlaces = json.encodeToString<List<AccessibleLocalMarker>>(places)
            val fileId = driveService.listFiles(INCLUSIMAP_PLACE_DATA_FOLDER_ID).find {
                it.name == "places.json"
            }?.id ?: throw IllegalStateException("File: places.json not found")
            driveService.updateFile(
                fileId,
                "places.json",
                updatedPlaces.toByteArray().inputStream(),
            )
        }.also {
            println("File uploaded successfully with new place: $accessibleLocal")
        }
    }

    override suspend fun updateAccessibleLocal(accessibleLocal: AccessibleLocalMarker) {
        withContext(Dispatchers.IO) {
            val places = getAccessibleLocals().toMutableList()
            places.removeIf { it.id == accessibleLocal.id }
            places.add(accessibleLocal)
            val updatedPlaces = json.encodeToString<List<AccessibleLocalMarker>>(places)
            val fileId = driveService.listFiles(INCLUSIMAP_PLACE_DATA_FOLDER_ID).find {
                it.name == "places.json"
            }?.id ?: throw IllegalStateException("File: places.json not found")
            driveService.updateFile(
                fileId,
                "places.json",
                updatedPlaces.toByteArray().inputStream(),
            )
        }.also {
            println("File uploaded successfully with updated place: $accessibleLocal")
        }
    }

    override suspend fun deleteAccessibleLocal(id: String) {
        withContext(Dispatchers.IO) {
            val places = getAccessibleLocals().toMutableList()
            places.removeIf { it.id == id }
            val updatedPlaces = json.encodeToString<List<AccessibleLocalMarker>>(places)
            val fileId = driveService.listFiles(INCLUSIMAP_PLACE_DATA_FOLDER_ID).find {
                it.name == "places.json"
            }?.id ?: throw IllegalStateException("File: places.json not found")
            driveService.updateFile(
                fileId,
                "places.json",
                updatedPlaces.toByteArray().inputStream(),
            )
        }.also {
            println("File uploaded successfully with removed place id: $id")
        }
    }
}
