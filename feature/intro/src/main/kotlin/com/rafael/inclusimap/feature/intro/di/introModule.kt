package com.rafael.inclusimap.feature.intro.di

import androidx.room.Room
import com.rafael.inclusimap.feature.intro.data.database.AppIntroDatabase
import com.rafael.inclusimap.feature.intro.data.repository.AppIntroRepositoryImpl
import com.rafael.inclusimap.feature.intro.presentation.viewmodel.AppIntroViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val introModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            AppIntroDatabase::class.java,
            AppIntroDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }
    single {
        AppIntroRepositoryImpl(get<AppIntroDatabase>().appIntroDao())
    }
    viewModel {
        AppIntroViewModel(get<AppIntroRepositoryImpl>())
    }
}
