package com.rafael.inclusimap.feature.auth.di

import androidx.room.Room
import com.rafael.inclusimap.feature.auth.data.database.AuthDatabase
import com.rafael.inclusimap.feature.auth.data.repository.LoginRepositoryImpl
import com.rafael.inclusimap.feature.auth.presentation.viewmodel.LoginViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            AuthDatabase::class.java,
            AuthDatabase.DATABASE_NAME,
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }
    single {
        LoginRepositoryImpl(get<AuthDatabase>().loginDao())
    }

    viewModel {
        LoginViewModel(get<LoginRepositoryImpl>())
    }
}
