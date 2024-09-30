plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.library.compose)
    alias(libs.plugins.rafael.spotless)
}

android.namespace = "com.rafael.inclusimap.feature.map_search"

dependencies {
    // Google Maps
    implementation(libs.maps.compose)
    // Koin
    api(libs.koin.core)
    implementation(libs.koin.core.viewmodel)
    // Projects
    implementation(projects.core.domain)
    implementation(projects.core.resources)
}
