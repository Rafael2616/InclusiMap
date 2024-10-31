plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.spotless)
}

android.namespace = "com.rafael.inclusimap.core.services"

dependencies {
    // Google drive
    api(libs.google.api.client)
    api(libs.google.api.services.drive)
    // Places Api
    implementation(libs.places)
    // Google Maps
    implementation(libs.maps.compose)
    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.koin.core.viewmodel)
    // Kotlin
    implementation(libs.kotlinx.coroutines)

    // Projects
    implementation(projects.core.domain)
}
