import com.android.build.api.dsl.LibraryExtension
import com.rafael.gradle.configureKotlinAndroid
import com.rafael.gradle.configureKotlinMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

@Suppress("unused")
class MultiplatformLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.multiplatform")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
            }

            plugins.withId("org.jetbrains.kotlin.multiplatform") {
                configureKotlinMultiplatform()
            }
        }
    }
}
