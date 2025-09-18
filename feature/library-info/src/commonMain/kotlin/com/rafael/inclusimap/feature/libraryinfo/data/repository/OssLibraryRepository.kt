package com.rafael.inclusimap.feature.libraryinfo.data.repository

import com.rafael.inclusimap.feature.libraryinfo.domain.model.OssLibrary
import kotlinx.coroutines.flow.StateFlow

expect class OssLibraryRepository() {
    val ossLibraries: StateFlow<List<OssLibrary>>
}
