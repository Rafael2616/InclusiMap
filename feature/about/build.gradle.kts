plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.library.compose)
    alias(libs.plugins.rafael.spotless)
}

android {
    namespace = "com.rafael.inclusimap.feature.about"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "VERSION_NAME", "\"${libs.versions.versionName.get()}\"")
    }
}

dependencies {
    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.compose.core)
    implementation(libs.coil.network)
    implementation(libs.ktor.client.android)

    // Projects
    implementation(projects.core.resources)
    implementation(projects.feature.intro)
}
