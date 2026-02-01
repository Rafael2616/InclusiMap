package com.rafael.inclusimap.core.di

import org.koin.core.context.startKoin

fun start() {
    startKoin {
        modules(modules)
    }
}
