package com.rafael.inclusimap.core.services.di

import com.rafael.inclusimap.core.services.AwsFileApiService
import com.rafael.inclusimap.core.services.PlacesApiService
import com.rafael.inclusimap.core.services.ktorClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val serviceModule = module {
    single {
        HttpClient(ktorClientEngine) {
            install(Logging) {
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                }
            }
        }
    }
    single { AwsFileApiService(get()) }
    single { PlacesApiService() }
}
