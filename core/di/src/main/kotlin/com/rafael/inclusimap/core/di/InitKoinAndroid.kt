package com.rafael.inclusimap.core.di

import android.content.Context
import com.rafael.inclusimap.core.services.di.serviceModule
import com.rafael.inclusimap.feature.auth.di.authModule
import com.rafael.inclusimap.feature.contributions.di.modules.contributionsModule
import com.rafael.inclusimap.feature.contributions.di.modules.libraryInfoModule
import com.rafael.inclusimap.feature.intro.di.introModule
import com.rafael.inclusimap.feature.map.di.mapModule
import com.rafael.inclusimap.feature.map.search.di.mapSearchModule
import com.rafael.inclusimap.feature.report.di.modules.reportModule
import com.rafael.inclusimap.feature.settings.di.settingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

fun initKoinAndroid(context: Context) {
    startKoin {
        androidContext(context)
        modules(
            serviceModule,
            authModule,
            introModule,
            mapModule,
            mapSearchModule,
            settingsModule,
            libraryInfoModule,
            contributionsModule,
            reportModule,
        )
        androidLogger(Level.INFO)
    }
}
