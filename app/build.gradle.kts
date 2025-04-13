import app.cash.licensee.LicenseeTask
import app.cash.licensee.SpdxId
import app.cash.licensee.UnusedAction.IGNORE
import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import java.util.Properties

plugins {
    alias(libs.plugins.rafael.application)
    alias(libs.plugins.rafael.application.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.licensee)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.rafael.inclusimap"

    defaultConfig {
        applicationId = "com.rafael.inclusimap"
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.targetSdk
                .get()
                .toInt()
        versionCode =
            libs.versions.versionCode
                .get()
                .toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    val releaseSigningFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    val releaseSigningConfig =
        if (releaseSigningFile.exists()) {
            keystoreProperties.load(releaseSigningFile.inputStream())
            signingConfigs.create("release") {
                storeFile = file(keystoreProperties.getProperty("KEYSTORE"))
                storePassword = keystoreProperties.getProperty("KEYSTORE_PASSWORD")
                keyAlias = keystoreProperties.getProperty("KEY_ALIAS")
                keyPassword = keystoreProperties.getProperty("KEY_PASSWORD")
                enableV3Signing = true
            }
        } else {
            signingConfigs.create("release") {
                storeFile = file("inclusimap.jks")
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
                enableV3Signing = true
            }
        }

    buildTypes {
        named("debug") {
            signingConfig = releaseSigningConfig
        }

        named("release") {
            isMinifyEnabled = true
            signingConfig = releaseSigningConfig
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    applicationVariants.all {
        outputs.all {
            (this as? ApkVariantOutputImpl)?.outputFileName =
                "InclusiMap v$versionName.apk"
        }
    }
    androidComponents.onVariants { variant ->
        val variantNameCapt = variant.name.replaceFirstChar { it.uppercase() }
        val licenseeTask = tasks.named<LicenseeTask>("licenseeAndroid$variantNameCapt")
        val copyArtifactsTask =
            tasks.register<Copy>("copy${variantNameCapt}Artifacts") {
                dependsOn(licenseeTask)
                from(licenseeTask.map { it.outputDir.file("artifacts.json") })
                into(layout.buildDirectory.dir("generated/dependencyAssets/${variant.name}"))
            }
        variant.sources.assets?.addGeneratedSourceDirectory(licenseeTask) {
            objects.directoryProperty().fileProvider(copyArtifactsTask.map { it.destinationDir })
        }
    }
}

dependencies {
    baselineProfile(projects.baselineProfile)
    implementation(libs.androidx.profileinstaller)
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    // Projects
    implementation(projects.core.navigationImpl)
    implementation(projects.core.di)
    implementation(projects.core.resources)
}

baselineProfile {
    // Don't build on every iteration of a full assemble.
    // Instead enable generation directly for the release build variant.
    automaticGenerationDuringBuild = false
}

licensee {
    allow(SpdxId.Apache_20)
    allow(SpdxId.MIT)
    allow(SpdxId.BSD_3_Clause)
    allowUrl("https://developer.android.com/studio/terms.html")
    allowUrl("https://cloud.google.com/maps-platform/terms/")

    unusedAction(IGNORE)
}
