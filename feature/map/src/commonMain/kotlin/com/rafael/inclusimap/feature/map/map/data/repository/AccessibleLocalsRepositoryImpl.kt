package com.rafael.inclusimap.feature.map.map.data.repository

import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.core.util.map.Constants.INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID
import com.rafael.inclusimap.core.util.map.extractPlaceID
import com.rafael.inclusimap.core.util.map.model.AccessibleLocalMarker
import com.rafael.inclusimap.feature.map.map.data.dao.AccessibleLocalsDao
import com.rafael.inclusimap.feature.map.map.domain.model.AccessibleLocalsEntity
import com.rafael.inclusimap.feature.map.map.domain.repository.AccessibleLocalsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class AccessibleLocalsRepositoryImpl(
    private val driveService: GoogleDriveService,
    private val dao: AccessibleLocalsDao,
) : AccessibleLocalsRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    override suspend fun getAccessibleLocals(): List<AccessibleLocalMarker>? = withContext(Dispatchers.IO) {
        val places = mutableListOf<AccessibleLocalMarker>()
        driveService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID).onSuccess { result ->
            result.map { file ->
                async {
                    driveService.getFileContent(file.id)?.decodeToString()?.let { content ->
                        try {
                            places.add(
                                json.decodeFromString<AccessibleLocalMarker>(
                                    content,
                                ),
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }.awaitAll()
        }
        return@withContext places
    }

    override suspend fun saveAccessibleLocal(accessibleLocal: AccessibleLocalMarker): String? = withContext(Dispatchers.IO) {
        val updatedPlace = json.encodeToString(accessibleLocal)
        driveService.createFile(
            accessibleLocal.id + "_" + accessibleLocal.authorEmail + ".json",
            updatedPlace,
            INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID,
        ).also {
            println("File uploaded successfully with new place: $accessibleLocal")
        }
    }

    override suspend fun updateAccessibleLocal(accessibleLocal: AccessibleLocalMarker) {
        withContext(Dispatchers.IO) {
            val updatedPlace = json.encodeToString(accessibleLocal)
            driveService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID)
                .onSuccess { result ->
                    val fileId =
                        result.find {
                            it.name.extractPlaceID()?.removeSuffix(".json") == accessibleLocal.id
                        }?.id
                            ?: return@withContext
                    driveService.updateFile(
                        fileId,
                        accessibleLocal.id + "_" + accessibleLocal.authorEmail + ".json",
                        updatedPlace.encodeToByteArray(),
                    )
                    println("File uploaded successfully with updated place: $accessibleLocal")
                }
        }
    }

    override suspend fun deleteAccessibleLocal(id: String) {
        withContext(Dispatchers.IO) {
            driveService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID)
                .onSuccess { result ->
                    val place = result.find { it.name.extractPlaceID() == id }
                    driveService.deleteFile(place?.id ?: return@withContext)
                    println("File uploaded successfully with removed place id: $id")
                }
        }
    }

    override suspend fun getAccessibleLocalsStored(id: Int): AccessibleLocalsEntity? = dao.getLocals(id)

    override suspend fun updateAccessibleLocalStored(accessibleLocalEntity: AccessibleLocalsEntity) {
        dao.updateLocals(accessibleLocalEntity)
    }
}
