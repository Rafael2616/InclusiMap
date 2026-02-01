plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.rafael.spotless)
}

kotlin {
    android {
        namespace = "com.rafael.inclusimap.core.resources"
        compileSdk = libs.versions.compileSdk.get().toInt()
        androidResources.enable = true
    }
}

compose.resources {
    generateResClass = always
    packageOfResClass = "com.rafael.inclusimap.core.resources"
    publicResClass = true
}
