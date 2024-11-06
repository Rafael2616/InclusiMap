package com.rafael.inclusimap.feature.auth.di

import androidx.room.Room
import com.rafael.inclusimap.feature.auth.data.database.AuthDatabase
import com.rafael.inclusimap.feature.auth.data.database.AuthMigrations
import com.rafael.inclusimap.feature.auth.data.repository.LoginRepositoryImpl
import com.rafael.inclusimap.feature.auth.domain.utils.MailerSenderClient
import com.rafael.inclusimap.feature.auth.presentation.viewmodel.LoginViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            AuthDatabase::class.java,
            AuthDatabase.DATABASE_NAME,
        )
            .addMigrations(
                AuthMigrations.migration1To2,
                AuthMigrations.migration2To3,
                AuthMigrations.migration3To4,
            )
            .fallbackToDestructiveMigration(true)
            .build()
    }
    single {
        LoginRepositoryImpl(get<AuthDatabase>().loginDao())
    }

    single {
        HttpClient(Android.create()) {
            install(Logging) {
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(
                    json = Json { ignoreUnknownKeys = true },
                )
            }
        }
    }

    single {
        MailerSenderClient(get())
    }

    viewModel {
        LoginViewModel(
            get<LoginRepositoryImpl>(),
            get<MailerSenderClient>(),
        )
    }
}
