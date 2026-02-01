package com.rafael.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.compose.ComposeExtension

internal fun Project.configureComposeMultiplatform() {
        kotlinMultiplatformExtension {
            composeMultiplatformExtension {
                sourceSets {
                    commonMain.dependencies {
                        implementation(libs.findLibrary("jetbrains-compose-foundation").get())
                        implementation(libs.findLibrary("jetbrains-compose-material3").get())
                        implementation(libs.findLibrary("jetbrains-compose-material-icons-extended").get())
                        implementation(libs.findLibrary("jetbrains-compose-runtime").get())
                        implementation(libs.findLibrary("jetbrains-compose-ui").get())
                        implementation(libs.findLibrary("jetbrains-compose-animation").get())
                        implementation(libs.findLibrary("jetbrains-compose-resources").get())
                        implementation(libs.findLibrary("jetbrains-compose-ui-tooling-preview").get())
                        implementation(libs.findLibrary("jetbrains-compose-backhandler").get())
                        implementation(libs.findLibrary("androidx-lifecycle-viewmodel").get())
                    }
                    androidMain.dependencies {
                        implementation(libs.findLibrary("jetbrains-compose-ui-tooling-preview").get())
                        implementation(libs.findLibrary("jetbrains-compose-ui-tooling").get())
                    }
                }
            }
        }
    }

fun Project.composeMultiplatformExtension(block: ComposeExtension.() -> Unit) {
    extensions.configure<ComposeExtension>(block)
}
