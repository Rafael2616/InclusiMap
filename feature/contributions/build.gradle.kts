plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.library.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.rafael.inclusimap.feature.contributions"

dependencies {
    // AndroidX
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation)
    implementation(libs.exifInterface)
    // Kotlin
    implementation(libs.kotlinx.serialization.json)
    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.core.viewmodel)
    // Lottie
    implementation(libs.lottie.compose)

    // Projects
    implementation(projects.core.domain)
    implementation(projects.core.navigation)
    implementation(projects.core.resources)
    implementation(projects.core.services)
    implementation(projects.feature.auth)
}
