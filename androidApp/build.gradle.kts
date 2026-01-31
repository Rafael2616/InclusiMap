import app.cash.licensee.LicenseeTask
import app.cash.licensee.SpdxId
import app.cash.licensee.UnusedAction.LOG
import java.util.Properties

plugins {
    alias(libs.plugins.rafael.android.application)
    alias(libs.plugins.rafael.android.application.compose)
    alias(libs.plugins.rafael.spotless)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.licensee)
}

android {
    namespace = "com.rafael.inclusimap"

    defaultConfig {
        applicationId = "com.rafael.inclusimap"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
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

    base.archivesName = "InclusiMap v${libs.versions.versionName.get()}"

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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)

    // Projects
    implementation(projects.core.navigation)
    implementation(projects.core.di)
}

licensee {
    allow(SpdxId.Apache_20)
    allow(SpdxId.MIT)
    allowUrl("https://opensource.org/license/mit")
    allowUrl("https://developer.android.com/studio/terms.html")
    allowUrl("https://cloud.google.com/maps-platform/terms/")
    allowUrl("https://github.com/vinceglb/FileKit/blob/main/LICENSE")
    allowUrl("https://github.com/icerockdev/moko-permissions/blob/master/LICENSE.md")

    unusedAction(LOG)
}
