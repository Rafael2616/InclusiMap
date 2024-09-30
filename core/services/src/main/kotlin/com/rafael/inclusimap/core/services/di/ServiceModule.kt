package com.rafael.inclusimap.core.services.di

import com.rafael.inclusimap.core.services.GoogleDriveService
import org.koin.dsl.module

val serviceModule = module {
    single {
        GoogleDriveService()
    }
}
