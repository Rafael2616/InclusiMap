plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.library.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.rafael.inclusimap.feature.map"

dependencies {
    // Kotlin
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    //AndroidX
    implementation(libs.google.accompanist.permissions)
    implementation(libs.androidx.activity.compose)
    //Google Maps
    api(libs.maps.compose)
    implementation(libs.play.services.location)
    // Koin
    api(libs.koin.core)
    implementation(libs.koin.core.viewmodel)

    // Projects
    implementation(projects.core.domain)
    implementation(projects.core.resources)
    implementation(projects.core.services)
    implementation(projects.core.settings)
    implementation(projects.feature.auth)
    implementation(projects.feature.mapSearch)
    implementation(projects.feature.intro)
}
