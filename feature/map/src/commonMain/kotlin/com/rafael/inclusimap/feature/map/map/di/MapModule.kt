package com.rafael.inclusimap.feature.map.map.di

import com.rafael.inclusimap.feature.contributions.data.repository.ContributionsRepositoryImpl
import com.rafael.inclusimap.feature.map.map.data.repository.AccessibleLocalsRepositoryImpl
import com.rafael.inclusimap.feature.map.map.data.repository.InclusiMapRepositoryImpl
import com.rafael.inclusimap.feature.map.map.presentation.viewmodel.InclusiMapGoogleMapViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mapModule = module {
    viewModel {
        InclusiMapGoogleMapViewModel(
            get<AccessibleLocalsRepositoryImpl>(),
            get<InclusiMapRepositoryImpl>(),
            get(),
            get<ContributionsRepositoryImpl>(),
        )
    }
}
