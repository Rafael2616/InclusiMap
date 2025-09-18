package io.github.agropec.core.navigation

import androidx.compose.ui.window.ComposeUIViewController
import com.rafael.inclusimap.core.navigation.InclusiMapNavHost
import org.koin.compose.KoinApplication

fun inclusimapNavHost() = ComposeUIViewController {
    KoinApplication(
        application = { modules(modules) },
    ) {
        InclusiMapNavHost()
    }
}
