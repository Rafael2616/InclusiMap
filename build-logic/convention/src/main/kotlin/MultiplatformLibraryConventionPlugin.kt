import com.rafael.gradle.configureKotlinMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class MultiplatformLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.kotlin.multiplatform.library")
            }
            configureKotlinMultiplatform()
        }
    }
}
