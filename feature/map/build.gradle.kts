plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.cocoapods)
}

android.namespace = "com.rafael.inclusimap.feature.map"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.jetbrains.navigation.compose)
            implementation(libs.androidx.room.runtime)
            implementation(libs.reveal.compose)
            implementation(libs.reveal.compose.shapes)
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.coil.compose)
            implementation(libs.zoomable.compose)
            implementation(libs.filekit.core)
            implementation(libs.filekit.compose)

            implementation(projects.core.ui)
            implementation(projects.core.util)
            implementation(projects.core.resources)
            implementation(projects.core.services)
            implementation(projects.feature.auth)
            implementation(projects.feature.mapSearch)
            implementation(projects.feature.contributions)
            implementation(projects.feature.report)
            implementation(projects.libs.mapsInterop)
        }
        androidMain.dependencies {
            implementation(libs.maps.compose)
        }
    }

    cocoapods {
        summary = "Inclusimap map module"
        homepage = "No links"
        version = "1.0"
        ios.deploymentTarget = "18.2"
        name = "map"
        podfile = rootProject.file("iosApp/Podfile")

        framework {
            baseName = "map"
            isStatic = true
        }

        pod("GoogleMaps") {
            version = libs.versions.pods.google.maps.get()
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
    }
}
