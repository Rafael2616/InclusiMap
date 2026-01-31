package com.rafael.inclusimap.feature.report.di.modules

import com.rafael.inclusimap.feature.report.presentation.viewmodel.ReportViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val reportModule = module {
    viewModel {
        ReportViewModel(get())
    }
}
