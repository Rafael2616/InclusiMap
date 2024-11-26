plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.rafael.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.rafael.inclusimap.core.navigation_impl"

dependencies {
    // AndroidX
    implementation(libs.androidx.navigation)
    // Koin
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.androidx.compose)
    // Material Motion
    implementation(libs.material.motion.compose)
    // Reveal
    implementation(libs.reveal.compose)
    // Kotlin
    implementation(libs.kotlinx.serialization.json)

    // Projects
    implementation(projects.core.domain)
    implementation(projects.core.navigation)
    implementation(projects.core.settings)
    implementation(projects.core.ui)
    implementation(projects.feature.auth)
    implementation(projects.feature.about)
    implementation(projects.feature.intro)
    implementation(projects.feature.map)
    implementation(projects.feature.mapSearch)
    implementation(projects.feature.report)
    implementation(projects.feature.settings)
    implementation(projects.feature.libraryInfo)
    implementation(projects.feature.contributions)
}
