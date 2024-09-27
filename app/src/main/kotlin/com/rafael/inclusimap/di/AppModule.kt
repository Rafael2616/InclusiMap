package com.rafael.inclusimap.di

import androidx.room.Room
import com.rafael.inclusimap.data.GoogleDriveService
import com.rafael.inclusimap.data.database.AppDatabase
import com.rafael.inclusimap.data.repository.AppIntroRepositoryImpl
import com.rafael.inclusimap.ui.viewmodel.AppIntroViewModel
import com.rafael.inclusimap.ui.viewmodel.InclusiMapGoogleMapScreenViewModel
import com.rafael.inclusimap.ui.viewmodel.LoginViewModel
import com.rafael.inclusimap.ui.viewmodel.PlaceDetailsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .build()
    }
    single {
        AppIntroRepositoryImpl(get<AppDatabase>().appIntroDao())
    }
    single {
        GoogleDriveService()
    }
    viewModel {
        LoginViewModel()
    }
    viewModel {
        AppIntroViewModel(get<AppIntroRepositoryImpl>())
    }
    viewModel {
        InclusiMapGoogleMapScreenViewModel()
    }
    viewModel {
        PlaceDetailsViewModel(get())
    }
}
