package com.rafael.inclusimap.feature.map.search.di

import androidx.room.Room
import com.rafael.inclusimap.feature.map.search.data.database.MapSearchDatabase
import com.rafael.inclusimap.feature.map.search.data.repository.MapSearchRepositoryImpl
import com.rafael.inclusimap.feature.map.search.presentation.viewmodel.SearchViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mapSearchModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            MapSearchDatabase::class.java,
            MapSearchDatabase.DATABASE_NAME,
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }
    single {
        MapSearchRepositoryImpl(get<MapSearchDatabase>().mapSearchDao())
    }

    viewModel {
        SearchViewModel(get<MapSearchRepositoryImpl>())
    }
}
