package com.rafael.inclusimap

import android.app.Application
import com.rafael.inclusimap.core.di.initKoinAndroid

class InclusiMapApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this@InclusiMapApp)
    }
}
