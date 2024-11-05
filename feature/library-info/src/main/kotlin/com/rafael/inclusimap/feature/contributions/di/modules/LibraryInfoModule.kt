package com.rafael.inclusimap.feature.contributions.di.modules

import com.rafael.inclusimap.feature.contributions.data.repository.OssLibraryRepository
import com.rafael.inclusimap.feature.contributions.presentation.viewmodel.LibraryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val libraryInfoModule =
    module {
        single { OssLibraryRepository() }
        viewModel {
            LibraryViewModel(
                get<OssLibraryRepository>(),
            )
        }
    }
