plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.rafael.inclusimap.feature.contributions"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.coil.compose)

            // Projects
            implementation(projects.core.resources)
            implementation(projects.core.services)
            implementation(projects.core.ui)
            implementation(projects.core.util)
            implementation(projects.feature.auth)
            implementation(projects.libs.mapsInterop)
        }
        androidMain.dependencies {
            implementation(libs.lottie.compose)
        }
    }
}
