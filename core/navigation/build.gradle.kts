plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "com.rafael.inclusimap.core.navigation"
        compileSdk = libs.versions.compileSdk.get().toInt()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.jetbrains.navigation.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.material.motion.compose)
            implementation(libs.reveal.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.moko.permissions.compose)

            // Projects
            implementation(projects.core.util)
            implementation(projects.core.ui)
            implementation(projects.core.di)
            implementation(projects.feature.auth)
            implementation(projects.feature.about)
            implementation(projects.feature.intro)
            implementation(projects.feature.map)
            implementation(projects.feature.mapSearch)
            implementation(projects.feature.report)
            implementation(projects.feature.settings)
            implementation(projects.feature.libraryInfo)
            implementation(projects.feature.contributions)
            implementation(projects.libs.mapsInterop)
        }
    }
}
