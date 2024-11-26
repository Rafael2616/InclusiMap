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
    // Reveal
    implementation(libs.reveal.compose)
    implementation(libs.reveal.compose.shapes)
    // Koin
    api(libs.koin.core)
    implementation(libs.koin.core.viewmodel)
    implementation(libs.koin.core)

    // Projects
    api(projects.core.settings)
    implementation(projects.core.navigation)
    implementation(projects.core.domain)
    implementation(projects.core.ui)
    implementation(projects.feature.map)
    implementation(projects.feature.intro)
}
