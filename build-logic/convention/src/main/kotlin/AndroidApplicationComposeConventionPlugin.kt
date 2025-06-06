import com.android.build.api.dsl.ApplicationExtension
import com.rafael.gradle.configureComposeAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

@Suppress("unused")
class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<ApplicationExtension> {
                configureComposeAndroid()
            }
        }
    }
}
