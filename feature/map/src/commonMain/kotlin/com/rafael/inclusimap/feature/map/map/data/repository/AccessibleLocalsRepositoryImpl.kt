package com.rafael.inclusimap.feature.map.map.data.repository

import com.rafael.inclusimap.core.services.AwsFileApiService
import com.rafael.inclusimap.core.util.map.Constants.INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH
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
    private val awsService: AwsFileApiService,
    private val dao: AccessibleLocalsDao,
) : AccessibleLocalsRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    override suspend fun getAccessibleLocals(): List<AccessibleLocalMarker> =
        withContext(Dispatchers.IO) {
            val places = mutableListOf<AccessibleLocalMarker>()
            awsService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH)
                .onSuccess { result ->
                    result.map { fileName ->
                        async {
                            awsService.downloadFile("$INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH/$fileName")
                                .getOrNull()?.decodeToString()?.let { content ->
                                json.decodeFromString<AccessibleLocalMarker>(content)
                                try {
                                    places.add(
                                        json.decodeFromString<AccessibleLocalMarker>(content),
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

    override suspend fun saveAccessibleLocal(accessibleLocal: AccessibleLocalMarker): String =
        withContext(Dispatchers.IO) {
            val updatedPlace = json.encodeToString(accessibleLocal)
            val placeFilePath =
                "$INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH/${accessibleLocal.id}_${accessibleLocal.authorEmail}.json"
            awsService.uploadFile(
                placeFilePath,
                updatedPlace,
            ).getOrNull().also {
                println("File uploaded successfully with new place: $accessibleLocal")
            }
            placeFilePath
        }

    override suspend fun updateAccessibleLocal(accessibleLocal: AccessibleLocalMarker) {
        withContext(Dispatchers.IO) {
            val updatedPlace = json.encodeToString(accessibleLocal)
            awsService.uploadFile(
                " $INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH/" + accessibleLocal.id + "_" + accessibleLocal.authorEmail + ".json",
                updatedPlace,
            )
            println("File uploaded successfully with updated place: $accessibleLocal")
        }
    }

    override suspend fun deleteAccessibleLocal(id: String) {
        withContext(Dispatchers.IO) {
           awsService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH)
                    .onSuccess { result ->
                        val place = result.find { it.extractPlaceID() == id }
                    awsService.deleteFile("$INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH/$place")
                        println("Place deelted successfully. Id: $id")
                    }
        }
    }

    override suspend fun getAccessibleLocalsStored(id: Int): AccessibleLocalsEntity? =
        dao.getLocals(id)

    override suspend fun updateAccessibleLocalStored(accessibleLocalEntity: AccessibleLocalsEntity) {
        dao.updateLocals(accessibleLocalEntity)
    }
}
