plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.rafael.spotless)
}

kotlin {
    android {
        namespace = "com.rafael.inclusimap.feature.settings"
        compileSdk = libs.versions.compileSdk.get().toInt()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.reveal.compose)
            implementation(libs.reveal.compose.shapes)
            implementation(libs.androidx.room.runtime)
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.coil.compose)

            // Projects
            implementation(projects.core.ui)
            implementation(projects.core.util)
            implementation(projects.libs.mapsInterop)
        }
    }
}
