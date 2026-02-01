plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "com.rafael.inclusimap.feature.report"
        compileSdk = libs.versions.compileSdk.get().toInt()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)

            // Projects
            implementation(projects.core.services)
            implementation(projects.core.ui)
            implementation(projects.core.util)
        }
    }
}
