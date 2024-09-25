package com.rafael.inclusimap

import android.app.Application
import com.rafael.inclusimap.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class InclusiMapApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@InclusiMapApp)
            modules(appModule)
        }
    }
}
