plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.library.compose)
    alias(libs.plugins.rafael.spotless)
}

android.namespace = "com.rafael.inclusimap.feature.settings"

dependencies {
    // AndroidX
    implementation(libs.androidx.navigation)
    implementation(libs.exifInterface)

    // Koin
    api(libs.koin.core)
    implementation(libs.koin.core.viewmodel)
    implementation(libs.koin.core)

    // Projects
    api(projects.core.settings)
    api(projects.core.navigation)
    api(projects.feature.map)
    api(projects.core.domain)
    implementation(projects.feature.intro)
}
