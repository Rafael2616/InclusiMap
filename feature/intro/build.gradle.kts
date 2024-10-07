plugins {
    alias(libs.plugins.rafael.library)
    alias(libs.plugins.rafael.library.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

android.namespace = "com.rafael.inclusimap.feature.intro"

dependencies {
    // Room
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    // Koin
    api(libs.koin.core)
    implementation(libs.koin.core.viewmodel)
    implementation(libs.koin.android)
    // Scrollbar
    implementation(libs.lazycolumn.scrollbar)

    // Projects
    implementation(projects.core.domain)
    implementation(projects.core.resources)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

room {
    schemaDirectory("$projectDir/schemas")
}
