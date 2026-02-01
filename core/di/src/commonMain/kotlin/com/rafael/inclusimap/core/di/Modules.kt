package com.rafael.inclusimap.core.di

import com.rafael.inclusimap.core.di.database.entitiesModule
import com.rafael.inclusimap.core.di.network.networkModule
import com.rafael.inclusimap.core.services.di.serviceModule
import com.rafael.inclusimap.core.util.permissions.di.permissionsModule
import com.rafael.inclusimap.feature.auth.di.authModule
import com.rafael.inclusimap.feature.contributions.di.modules.contributionsModule
import com.rafael.inclusimap.feature.intro.di.introModule
import com.rafael.inclusimap.feature.libraryinfo.di.modules.libraryInfoModule
import com.rafael.inclusimap.feature.map.map.di.mapModule
import com.rafael.inclusimap.feature.map.placedetails.di.modules.placeDetailsModule
import com.rafael.inclusimap.feature.map.search.di.mapSearchModule
import com.rafael.inclusimap.feature.report.di.modules.reportModule
import com.rafael.inclusimap.feature.settings.di.settingsModule

val modules = listOf(
    serviceModule,
    authModule,
    introModule,
    mapModule,
    mapSearchModule,
    settingsModule,
    libraryInfoModule,
    contributionsModule,
    reportModule,
    placeDetailsModule,
    entitiesModule,
    networkModule,
    permissionsModule,
)
