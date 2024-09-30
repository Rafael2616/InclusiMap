// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidx.room).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.spotless).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.kotlin.serialization).apply(false)
    alias(libs.plugins.jetbrains.compose.compiler).apply(false)
   // alias(libs.plugins.licensee).apply(false)

    alias(libs.plugins.rafael.spotless).apply(false)
    alias(libs.plugins.rafael.library).apply(false)
    alias(libs.plugins.rafael.library.compose).apply(false)
    alias(libs.plugins.rafael.application).apply(false)
    alias(libs.plugins.rafael.application.compose).apply(false)
}