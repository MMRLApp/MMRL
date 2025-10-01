@file:Suppress("unused")

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class ApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = "com.android.application")
        apply(plugin = "org.jetbrains.kotlin.android")

        extensions.configure<ApplicationExtension> {
            compileSdk = COMPILE_SDK
            buildToolsVersion = BUILD_TOOLS_VERSION

            defaultConfig {
                minSdk = MIN_SDK
                targetSdk = COMPILE_SDK
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }
        }

        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }

        extensions.configure<KotlinAndroidProjectExtension> {
            jvmToolchain(21)

            sourceSets.all {
                languageSettings {
                    optIn("kotlin.ExperimentalStdlibApi")
                    optIn("kotlinx.coroutines.FlowPreview")
                }
            }
        }
    }
}