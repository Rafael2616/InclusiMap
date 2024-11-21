import com.diffplug.gradle.spotless.SpotlessExtension
import com.rafael.gradle.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

@Suppress("unused")
class SpotlessConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.diffplug.spotless")

             extensions.configure<SpotlessExtension> {
                kotlin {
                    ktlint()
                        .setEditorConfigPath(rootProject.file(".editorconfig"))
                        .customRuleSets(
                            listOf(
                                libs.findLibrary("ktlint-compose-rules").get().get().toString(),
                            ),
                        )
                    target("src/**/*.kt")
                }
                kotlinGradle {
                    ktlint()
                        .setEditorConfigPath(rootProject.file(".editorconfig"))
                    target("*.gradle.kts")
                }
            }
        }
    }
}
