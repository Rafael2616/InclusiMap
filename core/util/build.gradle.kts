plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "com.rafael.inclusimap.core.util"
        compileSdk = libs.versions.compileSdk.get().toInt()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.moko.permissions)
            implementation(libs.moko.permissions.location)
            // Projects
            implementation(projects.core.resources)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
