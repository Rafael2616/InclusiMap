package com.rafael.inclusimap.feature.libraryinfo.data.repository

import com.rafael.inclusimap.feature.libraryinfo.domain.model.OssLibrary
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

actual class OssLibraryRepository {

    private val kotlinxJson = Json {
        ignoreUnknownKeys = true
    }

    @OptIn(ExperimentalForeignApi::class)
    actual val ossLibraries: StateFlow<List<OssLibrary>> = flow {
        val filePath = NSBundle.mainBundle.pathForResource("artifacts", "json")
        requireNotNull(filePath) { "artifacts.json not found in iOS bundle" }

        val content = NSString.stringWithContentsOfFile(
            filePath,
            NSUTF8StringEncoding,
            null,
        ) ?: error("Unable to read artifacts.json")

        val ossLibraries = kotlinxJson.decodeFromString<List<OssLibrary>>(content)
            .asSequence()
            .filter { it.name != "unknown" }
            .distinctBy { "${it.groupId}:${it.artifactId}" }
            .sortedBy { it.name }
            .distinctBy { it.name }
            .toList()

        emit(ossLibraries)
    }.flowOn(Dispatchers.Default)
        .stateIn(MainScope(), SharingStarted.Lazily, emptyList())
}
