plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.spotless)
}

android.namespace = "com.rafael.inclusimap.core.di"

dependencies {
    // Koin
    implementation(libs.bundles.koin)

    // Projects
    implementation(projects.core.services)
    implementation(projects.feature.auth)
    implementation(projects.feature.intro)
    implementation(projects.feature.map)
    implementation(projects.feature.mapSearch)
    implementation(projects.feature.settings)
    implementation(projects.feature.libraryInfo)
    implementation(projects.feature.contributions)
}
