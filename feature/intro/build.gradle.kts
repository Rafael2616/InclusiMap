plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.rafael.spotless)
}

kotlin {
    android {
        namespace = "com.rafael.inclusimap.feature.intro"
        compileSdk = libs.versions.compileSdk.get().toInt()
        androidResources.enable = true
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.room.runtime)
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)

            // Projects
            implementation(projects.core.ui)
            implementation(projects.core.resources)
            implementation(projects.libs.lazyColumnScrollbar)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}

compose.resources {
    packageOfResClass = "com.rafael.inclusimap.feature.intro"
    generateResClass = always
}
