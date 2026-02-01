package com.rafael.inclusimap.feature.intro.di

import com.rafael.inclusimap.feature.intro.data.repository.AppIntroRepositoryImpl
import com.rafael.inclusimap.feature.intro.presentation.viewmodel.AppIntroViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val introModule = module {
    viewModel {
        AppIntroViewModel(get<AppIntroRepositoryImpl>())
    }
}
