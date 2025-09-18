package com.rafael.gradle

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureComposeAndroid(
    androidExtension: CommonExtension<*, *, *, *, *, *>
) {
    androidExtension.apply {
        // Just using the infrastructure
        composeMultiplatformExtension {
            val compose = dependencies
            dependencies {
                "implementation"(compose.foundation)
                "implementation"(compose.material3)
                "implementation"(compose.materialIconsExtended)
                "implementation"(compose.runtime)
                "implementation"(compose.ui)
                "implementation"(compose.uiUtil)
                "implementation"(compose.animation)
                "debugImplementation"(compose.uiTooling)
//                "debugImplementation"(libs.findLibrary("androidx-ui-test-manifest").get())
            }
        }
    }
}
