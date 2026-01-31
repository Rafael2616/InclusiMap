plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.kotlin.serialization)
}


kotlin {
    android {
        namespace = "com.rafael.inclusimap.feature.library_info"
        compileSdk = libs.versions.compileSdk.get().toInt()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)

            implementation(projects.core.ui)
        }
    }
}
