plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.rafael.inclusimap.core.navigation"

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
