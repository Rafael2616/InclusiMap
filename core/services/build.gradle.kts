plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.spotless)
}

android.namespace = "com.rafael.inclusimap.core.services"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.kotlinx.coroutines)

            implementation(projects.libs.mapsInterop)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.google.api.client)
            implementation(libs.google.api.services.drive)
            implementation(libs.places)
            implementation(libs.maps.compose)
        }
    }
}
