package com.rafael.inclusimap.core.di.database

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val inclusimapDBModule: Module = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            InclusiMapDatabase::class.java,
            InclusiMapDatabase.DATABASE_NAME,
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }
}
