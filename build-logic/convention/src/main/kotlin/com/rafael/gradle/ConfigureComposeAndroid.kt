package com.rafael.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureComposeAndroid() {
    // Just using the infrastructure
    composeMultiplatformExtension {
        dependencies {
            "implementation"(libs.findLibrary("jetbrains-compose-foundation").get())
            "implementation"(libs.findLibrary("jetbrains-compose-material3").get())
            "implementation"(libs.findLibrary("jetbrains-compose-material-icons-extended").get())
            "implementation"(libs.findLibrary("jetbrains-compose-runtime").get())
            "implementation"(libs.findLibrary("jetbrains-compose-ui").get())
            "implementation"(libs.findLibrary("jetbrains-compose-animation").get())
            "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel").get())

            "debugImplementation"(libs.findLibrary("jetbrains-compose-ui-tooling").get())
        }
    }
}
