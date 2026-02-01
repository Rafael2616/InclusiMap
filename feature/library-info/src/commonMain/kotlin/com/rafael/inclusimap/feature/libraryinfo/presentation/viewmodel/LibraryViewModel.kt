package com.rafael.inclusimap.feature.libraryinfo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.rafael.inclusimap.feature.libraryinfo.data.repository.OssLibraryRepository

class LibraryViewModel(
    libraryRepository: OssLibraryRepository,
) : ViewModel() {
    val ossLibraries = libraryRepository.ossLibraries
}
