@file:Suppress("unused")

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = "com.google.devtools.ksp")

        extensions.configure<KspExtension> {
            arg("room.incremental", "true")
            arg("room.expandProjection", "true")
            arg("room.schemaLocation", "$projectDir/schemas")
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        dependencies {
            "implementation"(libs.findLibrary("androidx.room.ktx").get())
            "implementation"(libs.findLibrary("androidx.room.runtime").get())
            "ksp"(libs.findLibrary("androidx.room.compiler").get())
        }
    }
}