import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `kotlin-dsl`
     alias(libs.plugins.spotless)
}

group = "com.rafael"

// Configure the build-logic plugins to target JDK 21
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    compileOnly(libs.android.gradle)
    compileOnly(libs.kotlin.gradle)
    compileOnly(libs.spotless.gradle)
    compileOnly(libs.composeCompiler.gradle)
    compileOnly(libs.compose.gradle)
}

gradlePlugin {
    // register the convention plugin
    plugins {
        register("spotless") {
            id = "com.rafael.spotless"
            implementationClass = "SpotlessConventionPlugin"
        }
        register("library") {
            id = "com.rafael.multiplatform.library"
            implementationClass = "MultiplatformLibraryConventionPlugin"
        }
        register("composeLibrary") {
            id = "com.rafael.compose.multiplatform.library"
            implementationClass = "MultiplatformLibraryComposeConventionPlugin"
        }
        register("application") {
            id = "com.rafael.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("composeApplication") {
            id = "com.rafael.compose.android.application"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "com.rafael.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
    }
}

extensions.configure<SpotlessExtension> {
    kotlin {
        ktlint()
            .customRuleSets(
                listOf(
                    libs.ktlint.compose.rules.get().toString(),
                ),
            )
        target("src/**/*.kt")
    }
    kotlinGradle {
        ktlint()
        target("*.gradle.kts")
    }
}
