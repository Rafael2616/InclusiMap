@file:Suppress("UnstableApiUsage")

rootProject.name = "InclusiMap"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

include(":app")

include(":core:di")
include(":core:domain")
include(":core:ui")
include(":core:resources")
include(":core:settings")
include(":core:navigation")
include(":core:navigation-impl")
include(":core:services")

include(":feature:auth")
include(":feature:about")
include(":feature:settings")
include(":feature:intro")
include(":feature:map")
include(":feature:map-search")
include(":feature:library-info")

include(":baseline-profile")
