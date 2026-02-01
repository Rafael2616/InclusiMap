package com.rafael.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKotlinMultiplatform() {
    kotlinMultiplatformExtension {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64(),
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = project.name.replaceFirstChar { it.uppercase() }.replace("-", "")
                isStatic = true
            }
        }

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
            jvmToolchain(21)
        }
    }
    // https://youtrack.jetbrains.com/issue/KT-73114
    project.afterEvaluate {
        extensions.findByType(KotlinMultiplatformExtension::class.java)?.apply {
            sourceSets.removeAll { sourceSet ->
                setOf("androidUnitTest").contains(sourceSet.name)
            }
        }
    }
}

fun Project.kotlinMultiplatformExtension(block: KotlinMultiplatformExtension.() -> Unit) {
    extensions.configure<KotlinMultiplatformExtension>(block)
}

//fun Project.androidLibrary(block: KotlinMultiplatformAndroidLibraryTarget.() -> Unit) {
//    extensions.configure<KotlinMultiplatformAndroidLibraryTarget>(block)
//}
//
//val Project.androidNamespace: String
//    get() = "com.rafael.inclusimap.$name"
