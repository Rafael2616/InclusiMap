package com.rafael.tictactoe.feature.libraryinfo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.rafael.tictactoe.feature.libraryinfo.data.repository.OssLibraryRepository

class LibraryViewModel(
    libraryRepository: OssLibraryRepository,
) : ViewModel() {

    val ossLibraries = libraryRepository.ossLibraries
}
