plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.library.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.rafael.inclusimap.feature.auth"

dependencies {
    // Room
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    // Google drive
    implementation(libs.google.api.client)
    implementation(libs.google.api.services.drive)
    // Kotlin
    implementation(libs.kotlinx.serialization.json)
    // Koin
    api(libs.koin.core)
    implementation(libs.koin.core.viewmodel)
    implementation(libs.koin.android)
    // Ktor
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)

    // Projects
    implementation(projects.core.domain)
    implementation(projects.core.services)
    implementation(projects.core.resources)
    implementation(projects.feature.intro)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

room {
    schemaDirectory("$projectDir/schemas")
}
