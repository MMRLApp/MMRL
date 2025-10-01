@file:Suppress("unused")

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = "com.android.application")
        apply(plugin = "org.jetbrains.kotlin.android")
        apply(plugin = "org.jetbrains.kotlin.plugin.compose")

        extensions.configure<ApplicationExtension> {
            buildFeatures {
                compose = true
            }
        }

        extensions.configure<KotlinAndroidProjectExtension> {
            sourceSets.all {
                languageSettings {
                    optIn("androidx.compose.material3.ExperimentalMaterial3Api")
                    optIn("androidx.compose.foundation.ExperimentalFoundationApi")
                    optIn("androidx.compose.foundation.layout.ExperimentalLayoutApi")
                }
            }
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        dependencies {
            "implementation"(libs.findLibrary("androidx.compose.material3").get())
            "implementation"(libs.findLibrary("androidx.compose.ui").get())
            "implementation"(libs.findLibrary("androidx.compose.ui.tooling.preview").get())
            "debugImplementation"(libs.findLibrary("androidx.compose.ui.tooling").get())
        }
    }
}