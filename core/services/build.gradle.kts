plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.spotless)
}

android.namespace = "com.rafael.inclusimap.core.services"

dependencies {
    // Google drive
    api(libs.google.api.client)
    api(libs.google.api.services.drive)
    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.core.viewmodel)
    // Kotlin
    implementation(libs.kotlinx.coroutines)

    // Projects
    implementation(projects.core.domain)
}
