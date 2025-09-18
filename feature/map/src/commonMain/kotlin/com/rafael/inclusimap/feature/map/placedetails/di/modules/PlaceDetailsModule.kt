package com.rafael.inclusimap.feature.map.placedetails.di.modules

import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.core.services.PlacesApiService
import com.rafael.inclusimap.feature.auth.data.repository.LoginRepositoryImpl
import com.rafael.inclusimap.feature.contributions.data.repository.ContributionsRepositoryImpl
import com.rafael.inclusimap.feature.map.placedetails.presentation.viewmodel.PlaceDetailsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val placeDetailsModule = module {
    viewModel {
        PlaceDetailsViewModel(
            get<GoogleDriveService>(),
            get<PlacesApiService>(),
            get<LoginRepositoryImpl>(),
            get<ContributionsRepositoryImpl>(),
        )
    }
}
