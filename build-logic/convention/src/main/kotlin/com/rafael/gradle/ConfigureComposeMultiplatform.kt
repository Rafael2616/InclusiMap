package com.rafael.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ExperimentalComposeLibrary

@OptIn(ExperimentalComposeLibrary::class)
internal fun Project.configureComposeMultiplatform() {
        kotlinMultiplatformExtension {
            composeMultiplatformExtension {
                val compose = dependencies
                with(sourceSets) {
                    commonMain.dependencies {
                        implementation(compose.foundation)
                        implementation(compose.material3)
                        implementation(compose.materialIconsExtended)
                        implementation(compose.runtime)
                        implementation(compose.ui)
                        implementation(compose.uiUtil)
                        implementation(compose.animation)
                        implementation(compose.components.resources)
                        implementation(compose.components.uiToolingPreview)
                        implementation(libs.findLibrary("jetbrains-compose-backhandler").get())
                        implementation(libs.findLibrary("jetbrains-lifecycle-compose").get())
                    }
                    androidMain.dependencies {
                        implementation(compose.preview)
                        implementation(compose.uiTooling)
                    }
                    androidUnitTest.dependencies {
                        implementation(compose.uiTest)
                    }
                    commonTest.dependencies {
                        implementation(compose.uiTest)
                    }
                }
            }
        }
    }

fun Project.composeMultiplatformExtension(block: ComposeExtension.() -> Unit) {
    extensions.configure<ComposeExtension>(block)
}
