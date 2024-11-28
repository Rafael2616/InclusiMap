package com.rafael.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureComposeAndroid() {
    plugins.withId("org.jetbrains.kotlin.android") {
        dependencies {
            "implementation"(libs.findLibrary("androidx-compose-material3").get())
            "implementation"(libs.findLibrary("androidx-compose-animation").get())
            "implementation"(libs.findLibrary("androidx-compose-ui").get())
            "implementation"(libs.findLibrary("androidx-compose-foundation").get())
            "implementation"(libs.findLibrary("androidx-compose-runtime").get())
            "implementation"(libs.findLibrary("androidx-compose-material-icons-extended").get())
            "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
            "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
            "implementation"(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())
            "debugImplementation"(libs.findLibrary("androidx-compose-ui-test-manifest").get())
            "androidTestImplementation"(libs.findLibrary("androidx-compose-ui-test-junit4").get())
        }
    }
}
