plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.rafael.inclusimap.feature.auth"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.room.runtime)
            implementation(libs.kotlinx.serialization.json)
            api(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.ktor.client.core)
            implementation(libs.filekit.compose)

            // Projects
            implementation(projects.core.ui)
            implementation(projects.core.util)
            implementation(projects.core.services)
            implementation(projects.core.resources)
        }
    }
}
