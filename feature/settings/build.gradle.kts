plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.rafael.spotless)
}

android.namespace = "com.rafael.inclusimap.feature.settings"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.reveal.compose)
            implementation(libs.reveal.compose.shapes)
            implementation(libs.androidx.room.runtime)
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)

            // Projects
            implementation(projects.core.ui)
            implementation(projects.core.util)
            implementation(projects.libs.mapsInterop)
        }
        androidMain.dependencies {
            implementation(libs.exifInterface)
        }
    }
}
