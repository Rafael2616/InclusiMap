package com.rafael.inclusimap.feature.contributions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.rafael.inclusimap.feature.contributions.data.repository.OssLibraryRepository

class LibraryViewModel(
    libraryRepository: OssLibraryRepository,
) : ViewModel() {
    val ossLibraries = libraryRepository.ossLibraries
}
