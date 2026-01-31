@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.rafael.multiplatform.library)
    alias(libs.plugins.rafael.multiplatform.library.compose)
    alias(libs.plugins.rafael.spotless)
}

object MySettings {
    val versionName: String = "2.2.0"
    val namespace = "my.nanihadesuka.lazycolumnscrollbar"
}

kotlin {
    android {
        namespace = MySettings.namespace
        compileSdk = libs.versions.compileSdk.get().toInt()
    }
}
