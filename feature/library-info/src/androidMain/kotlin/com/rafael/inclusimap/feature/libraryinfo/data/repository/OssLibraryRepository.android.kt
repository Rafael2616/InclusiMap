package com.rafael.inclusimap.feature.libraryinfo.data.repository

import android.app.Application
import com.rafael.inclusimap.feature.libraryinfo.domain.model.OssLibrary
import kotlin.io.bufferedReader
import kotlin.io.readText
import kotlin.use
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject

actual class OssLibraryRepository {
    private val application: Application by inject(Application::class.java)
    private val kotlinxJson =
        Json {
            ignoreUnknownKeys = true
        }
    actual val ossLibraries: StateFlow<List<OssLibrary>> =
        flow {
            val librariesFile =
                application.resources.assets
                    .open("artifacts.json")
                    .bufferedReader()
                    .use { it.readText() }
            val ossLibraries =
                kotlinxJson
                    .decodeFromString<List<OssLibrary>>(librariesFile)
                    .asSequence()
                    .filter { it.name != "unknown" }
                    .distinctBy { "${it.groupId}:${it.artifactId}" }
                    .distinctBy { it.name }
                    .sortedBy { it.name }
                    .toList()
            emit(ossLibraries)
        }.flowOn(Dispatchers.IO)
            .stateIn(MainScope(), SharingStarted.Lazily, emptyList())
}
