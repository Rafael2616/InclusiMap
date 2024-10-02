plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.library.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.rafael.inclusimap.core.domain"

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.maps.compose)
    implementation(libs.google.api.services.drive)
}
