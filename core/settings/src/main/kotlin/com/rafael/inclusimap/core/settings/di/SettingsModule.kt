package com.rafael.inclusimap.core.settings.di

import androidx.room.Room
import com.rafael.inclusimap.core.settings.data.database.Migrations
import com.rafael.inclusimap.core.settings.data.database.SettingsDatabase
import com.rafael.inclusimap.core.settings.data.repository.SettingsRepositoryImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val coreSettingsModule =
    module {
        single {
            Room
                .databaseBuilder(
                    androidApplication(),
                    SettingsDatabase::class.java,
                    SettingsDatabase.DATABASE_NAME,
                ).fallbackToDestructiveMigration(true)
                .addMigrations(Migrations.migration1To2)
                .build()
        }
        single {
            SettingsRepositoryImpl(
                get<SettingsDatabase>().settingsDao(),
            )
        }
    }
