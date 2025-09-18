plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.rafael.inclusimap.core.util"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.moko.permissions)
            implementation(libs.moko.permissions.location)
            implementation(libs.jetbrains.viewmodel.compose)
            // Projects
            implementation(projects.core.resources)
        }
        androidMain.dependencies {
            implementation(libs.google.api.services.drive)
            implementation(libs.exifInterface)
            implementation(libs.koin.android)
        }
    }
}
