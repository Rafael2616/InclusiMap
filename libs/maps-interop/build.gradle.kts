@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.rafael.libs.maps_interop"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.maps.compose)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
