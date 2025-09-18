plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.rafael.spotless)
}

android.namespace = "com.rafael.inclusimap.core.ui"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.reveal.compose.shapes)
        }
    }
}
