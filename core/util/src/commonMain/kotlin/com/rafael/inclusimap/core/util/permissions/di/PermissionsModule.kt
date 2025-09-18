package com.rafael.inclusimap.core.util.permissions.di

import com.rafael.inclusimap.core.util.permissions.PermissionsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val permissionsModule = module {
    viewModel {
        PermissionsViewModel(get())
    }
}
