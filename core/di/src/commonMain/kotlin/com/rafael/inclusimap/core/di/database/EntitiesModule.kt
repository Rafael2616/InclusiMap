package com.rafael.inclusimap.core.di.database

import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.feature.auth.data.repository.LoginRepositoryImpl
import com.rafael.inclusimap.feature.intro.data.repository.AppIntroRepositoryImpl
import com.rafael.inclusimap.feature.map.map.data.repository.AccessibleLocalsRepositoryImpl
import com.rafael.inclusimap.feature.map.map.data.repository.InclusiMapRepositoryImpl
import com.rafael.inclusimap.feature.map.search.data.repository.MapSearchRepositoryImpl
import com.rafael.inclusimap.feature.settings.data.repository.SettingsRepositoryImpl
import org.koin.dsl.module

val entitiesModule = module {
    includes(inclusimapDBModule)
    single {
        LoginRepositoryImpl(get<InclusiMapDatabase>().loginDao())
    }
    single {
        AccessibleLocalsRepositoryImpl(
            get<GoogleDriveService>(),
            get<InclusiMapDatabase>().accessibleLocalsDao(),
        )
    }
    single {
        InclusiMapRepositoryImpl(get<InclusiMapDatabase>().inclusiMapDao())
    }
    single {
        MapSearchRepositoryImpl(get<InclusiMapDatabase>().mapSearchDao())
    }
    single {
        SettingsRepositoryImpl(get<InclusiMapDatabase>().settingsDao())
    }
    single {
        AppIntroRepositoryImpl(get<InclusiMapDatabase>().appIntroDao())
    }
}
