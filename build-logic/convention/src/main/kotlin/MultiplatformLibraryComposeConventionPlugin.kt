import com.rafael.gradle.configureComposeMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class MultiplatformLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("org.jetbrains.compose")
            }
            configureComposeMultiplatform()
        }
    }
}
