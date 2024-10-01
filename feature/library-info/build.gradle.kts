plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.library.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.rafael.tictactoe.feature.library_info"

dependencies {
    //Kotlin
    implementation(libs.kotlinx.serialization.json)
    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.core.viewmodel)
}
