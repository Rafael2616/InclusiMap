package com.rafael.inclusimap.feature.map.map.di

import androidx.room.Room
import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.feature.auth.data.repository.LoginRepositoryImpl
import com.rafael.inclusimap.feature.contributions.data.repository.ContributionsRepositoryImpl
import com.rafael.inclusimap.feature.map.map.data.database.MapDatabase
import com.rafael.inclusimap.feature.map.map.data.repository.AccessibleLocalsRepositoryImpl
import com.rafael.inclusimap.feature.map.map.data.repository.InclusiMapRepositoryImpl
import com.rafael.inclusimap.feature.map.map.presentation.viewmodel.InclusiMapGoogleMapViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mapModule =
    module {
        single {
            Room
                .databaseBuilder(
                    androidApplication(),
                    MapDatabase::class.java,
                    MapDatabase.DATABASE_NAME,
                ).fallbackToDestructiveMigration(true)
                .build()
        }
        single {
            AccessibleLocalsRepositoryImpl(
                get<GoogleDriveService>(),
                get<MapDatabase>().accessibleLocalsDao(),
            )
        }
        single {
            InclusiMapRepositoryImpl(get<MapDatabase>().inclusiMapDao())
        }
        viewModel {
            InclusiMapGoogleMapViewModel(
                get<AccessibleLocalsRepositoryImpl>(),
                get<InclusiMapRepositoryImpl>(),
                get<GoogleDriveService>(),
                get<LoginRepositoryImpl>(),
                get<ContributionsRepositoryImpl>(),
            )
        }
    }
