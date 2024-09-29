package com.rafael.inclusimap.di

import androidx.room.Room
import com.rafael.inclusimap.data.GoogleDriveService
import com.rafael.inclusimap.data.database.AppDatabase
import com.rafael.inclusimap.data.repository.AccessibleLocalsRepositoryImpl
import com.rafael.inclusimap.data.repository.AppIntroRepositoryImpl
import com.rafael.inclusimap.data.repository.LoginRepositoryImpl
import com.rafael.inclusimap.settings.data.repository.SettingsRepositoryImpl
import com.rafael.inclusimap.ui.viewmodel.AppIntroViewModel
import com.rafael.inclusimap.ui.viewmodel.InclusiMapGoogleMapScreenViewModel
import com.rafael.inclusimap.ui.viewmodel.LoginViewModel
import com.rafael.inclusimap.ui.viewmodel.PlaceDetailsViewModel
import com.rafael.inclusimap.ui.viewmodel.SearchViewModel
import com.rafael.inclusimap.settings.presentation.viewmodel.SettingsViewModel
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
            .fallbackToDestructiveMigration(true)
            .build()
    }
    single {
        GoogleDriveService()
    }
    single {
        AppIntroRepositoryImpl(get<AppDatabase>().appIntroDao())
    }
    single {
        LoginRepositoryImpl(get<AppDatabase>().loginDao())
    }
    single {
        AccessibleLocalsRepositoryImpl(get<GoogleDriveService>())
    }
    single {
        SettingsRepositoryImpl(get<AppDatabase>().settingsDao())
    }
    viewModel {
        LoginViewModel(get<LoginRepositoryImpl>())
    }
    viewModel {
        AppIntroViewModel(get<AppIntroRepositoryImpl>())
    }
    viewModel {
        InclusiMapGoogleMapScreenViewModel(get<AccessibleLocalsRepositoryImpl>())
    }
    viewModel {
        PlaceDetailsViewModel(get(), get<LoginRepositoryImpl>())
    }
    viewModel {
        SearchViewModel()
    }
    viewModel {
        SettingsViewModel(get<SettingsRepositoryImpl>())
    }
}
