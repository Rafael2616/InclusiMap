plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.rafael.library.compose)
}

android.namespace = "com.rafael.inclusimap.core.navigation_impl"

dependencies {
    // Navigation
    implementation(libs.androidx.navigation)
    // Koin
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.androidx.compose)
    // Material Motion
    implementation(libs.material.motion.compose)

    implementation(libs.play.services.location)

    // Projects
    implementation(projects.core.navigation)
    implementation(projects.core.settings)
    implementation(projects.core.ui)
    implementation(projects.feature.auth)
    implementation(projects.feature.intro)
    implementation(projects.feature.map)
    implementation(projects.feature.mapSearch)
    implementation(projects.feature.settings)
}
