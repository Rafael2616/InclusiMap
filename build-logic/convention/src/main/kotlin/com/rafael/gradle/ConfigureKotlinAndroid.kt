package com.rafael.gradle

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project

internal fun Project.configureKotlinAndroid(
    androidExtension: CommonExtension<*, *, *, *, *, *>
) {
    androidExtension.apply {
        compileSdk = libs.findVersion("compileSdk").get().toString().toIntOrNull()
        defaultConfig {
            minSdk = libs.findVersion("minSdk").get().toString().toIntOrNull()
        }

        compileOptions {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }

        packaging.resources.excludes += listOf(
            "/META-INF/AL2.0",
            "/META-INF/INDEX.LIST",
            "/META-INF/LGPL2.1",
            "/META-INF/LICENSE.md",
            "/META-INF/LICENSE-notice.md",
            "/META-INF/DEPENDENCIES",
            "/META-INF/gradle/incremental.annotation.processors",
        )
    }
}
