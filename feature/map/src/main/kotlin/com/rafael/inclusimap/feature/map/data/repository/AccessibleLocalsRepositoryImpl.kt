package com.rafael.inclusimap.feature.map.data.repository

import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.network.onSuccess
import com.rafael.inclusimap.core.domain.util.Constants.INCLUSIMAP_PLACE_DATA_FOLDER_ID
import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.feature.map.domain.repository.AccessibleLocalsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.rafael.inclusimap.core.domain.network.Result.Error
import com.rafael.inclusimap.core.domain.network.Result.Success


class AccessibleLocalsRepositoryImpl(
    private val driveService: GoogleDriveService,
) : AccessibleLocalsRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    override suspend fun getAccessibleLocals(): List<AccessibleLocalMarker>? {
        return withContext(Dispatchers.IO) {
            when (val result = driveService.listFiles(INCLUSIMAP_PLACE_DATA_FOLDER_ID)) {
                is Success -> {
                    result.data
                        .find { it.name == "places.json" }
                        ?.let { file ->
                            driveService.getFileContent(file.id)?.decodeToString()?.let { content ->
                                try {
                                    json.decodeFromString<List<AccessibleLocalMarker>>(content)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        }
                }
                is Error -> {
                    emptyList()
                }
            }
        }
    }

    override suspend fun saveAccessibleLocal(accessibleLocal: AccessibleLocalMarker) {
        withContext(Dispatchers.IO) {
            val places = getAccessibleLocals()?.toMutableList()
            places?.add(accessibleLocal)
            val updatedPlaces = json.encodeToString(places ?: return@withContext)

            when (val result = driveService.listFiles(INCLUSIMAP_PLACE_DATA_FOLDER_ID)) {
                is Success -> {
                    val fileId = result.data.find { it.name == "places.json" }?.id
                        ?: throw IllegalStateException("File: places.json not found")
                    driveService.updateFile(
                        fileId,
                        "places.json",
                        updatedPlaces.toByteArray().inputStream(),
                    )
                }
                is Error -> {
                }
            }
        }.also {
            println("File uploaded successfully with new place: $accessibleLocal")
        }
    }

    override suspend fun updateAccessibleLocal(accessibleLocal: AccessibleLocalMarker) {
        withContext(Dispatchers.IO) {
            val places = getAccessibleLocals()?.toMutableList()
            places?.removeIf { it.id == accessibleLocal.id }
            places?.add(accessibleLocal)
            val updatedPlaces = json.encodeToString(places ?: return@withContext)

            when (val result = driveService.listFiles(INCLUSIMAP_PLACE_DATA_FOLDER_ID)) {
                is Success -> {
                    val fileId = result.data.find { it.name == "places.json" }?.id
                        ?: throw IllegalStateException("File: places.json not found")
                    driveService.updateFile(
                        fileId,
                        "places.json",
                        updatedPlaces.toByteArray().inputStream(),
                    )
                }
                is Error -> {
                }
            }
        }.also {
            println("File uploaded successfully with updated place: $accessibleLocal")
        }
    }

    override suspend fun deleteAccessibleLocal(id: String) {
        withContext(Dispatchers.IO) {
            val places = getAccessibleLocals()?.toMutableList()
            places?.removeIf { it.id == id }
            val updatedPlaces = json.encodeToString(places ?: return@withContext)

            when (val result = driveService.listFiles(INCLUSIMAP_PLACE_DATA_FOLDER_ID)) {
                is Success -> {
                    val fileId = result.data.find { it.name == "places.json" }?.id
                        ?: throw IllegalStateException("File: places.json not found")
                    driveService.updateFile(
                        fileId,
                        "places.json",
                        updatedPlaces.toByteArray().inputStream(),
                    )
                }
                is Error -> {
                }
            }
        }.also {
            println("File uploaded successfully with removed place id: $id")
        }
    }
}
