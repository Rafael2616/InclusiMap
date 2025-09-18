package com.rafael.inclusimap.feature.contributions.di.modules

import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.feature.auth.data.repository.LoginRepositoryImpl
import com.rafael.inclusimap.feature.contributions.data.repository.ContributionsRepositoryImpl
import com.rafael.inclusimap.feature.contributions.presentation.viewmodel.ContributionsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val contributionsModule = module {
    viewModel {
        ContributionsViewModel(
            get<LoginRepositoryImpl>(),
            get<GoogleDriveService>(),
            get<ContributionsRepositoryImpl>(),
        )
    }
    single {
        ContributionsRepositoryImpl(
            get<LoginRepositoryImpl>(),
            get<GoogleDriveService>(),
        )
    }
}
