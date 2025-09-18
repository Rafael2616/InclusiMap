plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

android.namespace = "com.rafael.inclusimap.core.di"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core.viewmodel)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.androidx.room.runtime)

            // Projects
            implementation(projects.core.services)
            implementation(projects.core.util)
            implementation(projects.feature.auth)
            implementation(projects.feature.intro)
            implementation(projects.feature.map)
            implementation(projects.feature.mapSearch)
            implementation(projects.feature.report)
            implementation(projects.feature.settings)
            implementation(projects.feature.libraryInfo)
            implementation(projects.feature.contributions)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.ios)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.ktor.client.android)
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}
