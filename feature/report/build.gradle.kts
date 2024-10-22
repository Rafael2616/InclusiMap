plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.library.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.rafael.inclusimap.feature.report"

dependencies {
    // Kotlin
    implementation(libs.kotlinx.serialization.json)
    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.core.viewmodel)

    // Projects
    implementation(projects.core.domain)
    implementation(projects.core.services)
    implementation(projects.feature.auth)
}
