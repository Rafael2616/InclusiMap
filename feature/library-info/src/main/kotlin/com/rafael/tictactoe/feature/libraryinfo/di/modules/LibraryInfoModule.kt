package com.rafael.tictactoe.feature.libraryinfo.di.modules

import com.rafael.tictactoe.feature.libraryinfo.data.repository.OssLibraryRepository
import com.rafael.tictactoe.feature.libraryinfo.presentation.viewmodel.LibraryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val libraryInfoModule = module {
    single { OssLibraryRepository() }
    viewModel {
        LibraryViewModel(
            get<OssLibraryRepository>(),
        )
    }
}
