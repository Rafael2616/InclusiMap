package com.rafael.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

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
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${composeCompilerReportsDir()}",
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${composeCompilerMetricsDir()}"
            )
        }
    }
}

private fun Project.composeCompilerMetricsDir() =
    "${layout.buildDirectory.get().asFile.absolutePath}/compose_metrics"

private fun Project.composeCompilerReportsDir() =
    "${layout.buildDirectory.get().asFile.absolutePath}/compose_reports"
