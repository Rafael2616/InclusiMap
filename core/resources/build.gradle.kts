plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.rafael.spotless)
}

android.namespace = "com.rafael.inclusimap.core.resources"

compose.resources {
    generateResClass = always
    packageOfResClass = "com.rafael.inclusimap.core.resources"
    publicResClass = true
}
