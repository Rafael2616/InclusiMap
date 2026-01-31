import com.android.build.api.dsl.ApplicationExtension
import com.rafael.gradle.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

@Suppress("unused")
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
            }
            extensions.configure<ApplicationExtension> {
                compileSdk {
                    version = release(libs.findVersion("compileSdk").get().toString().toInt())
                }

                defaultConfig {
                    minSdk = libs.findVersion("minSdk").get().toString().toInt()
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_21
                    targetCompatibility = JavaVersion.VERSION_21
                }

                packaging.resources.excludes += listOf(
                    "/META-INF/AL2.0",
                    "/META-INF/INDEX.LIST",
                    "/META-INF/LGPL2.1",
                    "/META-INF/LICENSE.md",
                    "/META-INF/LICENSE-notice.md",
                    "/META-INF/DEPENDENCIES",
                    "/META-INF/gradle/incremental.annotation.processors",
                )
            }
        }
    }
}
