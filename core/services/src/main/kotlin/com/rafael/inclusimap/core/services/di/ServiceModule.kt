package com.rafael.inclusimap.core.services.di

import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.core.services.PlacesApiService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val serviceModule =
    module {
        single {
            GoogleDriveService()
        }

        single {
            PlacesApiService(androidContext())
        }
    }
