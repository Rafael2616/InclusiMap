package com.rafael.inclusimap.feature.map.search.di

import com.rafael.inclusimap.feature.map.search.data.repository.MapSearchRepositoryImpl
import com.rafael.inclusimap.feature.map.search.presentation.viewmodel.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mapSearchModule = module {
    viewModel {
        SearchViewModel(get<MapSearchRepositoryImpl>())
    }
}
