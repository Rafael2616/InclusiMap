package com.rafael.inclusimap.core.services

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual val ktorClientEngine: HttpClientEngine = Darwin.create()
