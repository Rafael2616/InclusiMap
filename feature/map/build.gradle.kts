plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.library.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

android.namespace = "com.rafael.inclusimap.feature.map"

dependencies {
    // Kotlin
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    // AndroidX
    implementation(libs.google.accompanist.permissions)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation)
    implementation(libs.exifInterface)

    // Google Maps
    api(libs.maps.compose)
    implementation(libs.play.services.location)
    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    // Koin
    api(libs.koin.core)
    api(libs.koin.android)
    implementation(libs.koin.core.viewmodel)
    implementation(libs.zoomable.compose)

    // Projects
    implementation(projects.core.domain)
    implementation(projects.core.navigation)
    implementation(projects.core.resources)
    implementation(projects.core.services)
    implementation(projects.core.settings)
    implementation(projects.feature.auth)
    implementation(projects.feature.mapSearch)
    implementation(projects.feature.intro)
    implementation(projects.feature.contributions)
    implementation(projects.feature.report)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

room {
    schemaDirectory("$projectDir/schemas")
}
