package com.rafael.inclusimap.feature.settings.di

import com.rafael.inclusimap.core.settings.data.repository.SettingsRepositoryImpl
import com.rafael.inclusimap.core.settings.di.coreSettingsModule
import com.rafael.inclusimap.feature.settings.presentation.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    includes(coreSettingsModule)
    viewModel {
        SettingsViewModel(get<SettingsRepositoryImpl>())
    }
}

