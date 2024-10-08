package com.rafael.inclusimap.feature.map.di

import androidx.room.Room
import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.feature.auth.data.repository.LoginRepositoryImpl
import com.rafael.inclusimap.feature.map.data.database.MapDatabase
import com.rafael.inclusimap.feature.map.data.repository.AccessibleLocalsRepositoryImpl
import com.rafael.inclusimap.feature.map.presentation.viewmodel.InclusiMapGoogleMapViewModel
import com.rafael.inclusimap.feature.map.presentation.viewmodel.PlaceDetailsViewModel
import com.rafael.inclusimap.feature.map.presentation.viewmodel.ReportViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mapModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            MapDatabase::class.java,
            MapDatabase.DATABASE_NAME,
        )
    }
    single {
        AccessibleLocalsRepositoryImpl(get<GoogleDriveService>(), get<MapDatabase>().accessibleLocalsDao())
    }
    viewModel {
        InclusiMapGoogleMapViewModel(get<AccessibleLocalsRepositoryImpl>())
    }
    viewModel {
        PlaceDetailsViewModel(get(), get<LoginRepositoryImpl>())
    }
    viewModel {
        ReportViewModel(get<LoginRepositoryImpl>(), get<GoogleDriveService>())
    }
}
