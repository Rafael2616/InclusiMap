package com.rafael.inclusimap.feature.map.di

import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.feature.auth.data.repository.LoginRepositoryImpl
import com.rafael.inclusimap.feature.map.data.repository.AccessibleLocalsRepositoryImpl
import com.rafael.inclusimap.feature.map.presentation.viewmodel.InclusiMapGoogleMapViewModel
import com.rafael.inclusimap.feature.map.presentation.viewmodel.PlaceDetailsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mapModule = module {
    single {
        AccessibleLocalsRepositoryImpl(get<GoogleDriveService>())
    }
    viewModel {
        InclusiMapGoogleMapViewModel(get<AccessibleLocalsRepositoryImpl>())
    }
    viewModel {
        PlaceDetailsViewModel(get(), get<LoginRepositoryImpl>())
    }
}
