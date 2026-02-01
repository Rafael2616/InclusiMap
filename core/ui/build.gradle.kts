plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.rafael.spotless)
}

kotlin {
    android {
        namespace = "com.rafael.inclusimap.core.ui"
        compileSdk = libs.versions.compileSdk.get().toInt()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.reveal.compose.shapes)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}
