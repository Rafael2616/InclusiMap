package com.rafael.inclusimap.feature.libraryinfo.di.modules

import com.rafael.inclusimap.feature.libraryinfo.data.repository.OssLibraryRepository
import com.rafael.inclusimap.feature.libraryinfo.presentation.viewmodel.LibraryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val libraryInfoModule = module {
    single { OssLibraryRepository() }
    viewModel {
        LibraryViewModel(get<OssLibraryRepository>())
    }
}
