package com.rafael.inclusimap.feature.auth.di

import com.rafael.inclusimap.feature.auth.data.repository.LoginRepositoryImpl
import com.rafael.inclusimap.feature.auth.data.repository.MailerSenderClient
import com.rafael.inclusimap.feature.auth.presentation.viewmodel.LoginViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single { MailerSenderClient(get()) }

    viewModel {
        LoginViewModel(
            get<LoginRepositoryImpl>(),
            get<MailerSenderClient>(),
        )
    }
}
