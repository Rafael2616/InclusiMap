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

include(":androidApp")

include(":core:di")
include(":core:util")
include(":core:ui")
include(":core:resources")
include(":core:navigation")
include(":core:services")

include(":feature:auth")
include(":feature:about")
include(":feature:contributions")
include(":feature:report")
include(":feature:settings")
include(":feature:intro")
include(":feature:map")
include(":feature:map-search")
include(":feature:library-info")

include(":libs:lazyColumnScrollbar")
include(":libs:maps-interop")
