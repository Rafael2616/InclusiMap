package com.rafael.inclusimap.core.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

fun initKoinAndroid(context: Context) {
    startKoin {
        androidContext(context)
        modules(modules)
        androidLogger(Level.INFO)
    }
}
