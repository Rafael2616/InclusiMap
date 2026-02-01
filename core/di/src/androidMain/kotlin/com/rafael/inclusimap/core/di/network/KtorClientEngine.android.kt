package com.rafael.inclusimap.core.di.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android

actual val ktorClientEngine: HttpClientEngine = Android.create()
