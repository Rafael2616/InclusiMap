plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.rafael.spotless)
}

kotlin {
    android {
        namespace = "com.rafael.inclusimap.feature.about"
        compileSdk = libs.versions.compileSdk.get().toInt()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.coil.compose)
            implementation(libs.coil.network)

            // Projects
            implementation(projects.core.ui)
            implementation(projects.core.resources)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.ios)
        }
    }
}
