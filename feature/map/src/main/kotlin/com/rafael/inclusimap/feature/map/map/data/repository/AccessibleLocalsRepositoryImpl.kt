package com.rafael.inclusimap.feature.map.map.data.repository

import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.network.Result.Error
import com.rafael.inclusimap.core.domain.network.Result.Success
import com.rafael.inclusimap.core.domain.util.Constants.INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID
import com.rafael.inclusimap.core.domain.util.extractPlaceID
import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.feature.map.map.data.dao.AccessibleLocalsDao
import com.rafael.inclusimap.feature.map.map.domain.AccessibleLocalsEntity
import com.rafael.inclusimap.feature.map.map.domain.repository.AccessibleLocalsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
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
        when (
            val result =
                driveService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID)
        ) {
            is Success -> {
                result.data.map { file ->
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
                if (places.isEmpty()) null else places
            }

            is Error -> {
                places
            }
        }
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

            when (
                val result =
                    driveService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID)
            ) {
                is Success -> {
                    val fileId =
                        result.data.find { it.name.extractPlaceID()?.removeSuffix(".json") == accessibleLocal.id }?.id
                            ?: return@withContext
                    driveService.updateFile(
                        fileId,
                        accessibleLocal.id + "_" + accessibleLocal.authorEmail + ".json",
                        updatedPlace.toByteArray().inputStream(),
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
            when (
                val result =
                    driveService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID)
            ) {
                is Success -> {
                    val place = result.data.find { it.name.extractPlaceID() == id }

                    driveService.deleteFile(place?.id ?: return@withContext)
                }

                is Error -> {
                }
            }
        }.also {
            println("File uploaded successfully with removed place id: $id")
        }
    }

    override suspend fun getAccessibleLocalsStored(id: Int): AccessibleLocalsEntity? = dao.getLocals(id)

    override suspend fun updateAccessibleLocalStored(accessibleLocalEntity: AccessibleLocalsEntity) {
        dao.updateLocals(accessibleLocalEntity)
    }
}
