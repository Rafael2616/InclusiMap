plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.library.compose)
    alias(libs.plugins.rafael.spotless)
}

android.namespace = "com.rafael.inclusimap.core.ui"

dependencies {
    // AndroidX
    implementation(libs.androidx.activity.compose)
    // Reveal
    implementation(libs.reveal.compose.shapes)

    // Projects
    api(projects.core.settings)
}
