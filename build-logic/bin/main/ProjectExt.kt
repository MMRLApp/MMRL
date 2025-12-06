import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Properties

val Project.commitId: String get() = exec("git rev-parse --short HEAD")
val Project.commitCount: Int get() = exec("git rev-list --count HEAD").toInt()

fun Project.exec(command: String): String =
    providers
        .exec {
            commandLine(command.split(" "))
        }.standardOutput.asText
        .get()
        .trim()

val Project.releaseKeyStore: File get() = File(project.properties["keyStore"] as String)
val Project.releaseKeyStorePassword: String get() = project.properties["keyStorePassword"] as String
val Project.releaseKeyAlias: String get() = project.properties["keyAlias"] as String
val Project.releaseKeyPassword: String get() = project.properties["keyPassword"] as String
val Project.hasReleaseKeyStore: Boolean get() {
    gradleSigningProperties(rootDir).apply {
        stringPropertyNames().forEach {
            project.extra[it] = getProperty(it)
        }
    }

    return project.hasProperty("keyStore")
}

private fun gradleSigningProperties(rootDir: File): Properties {
    val properties = Properties()
    val signingProperties = rootDir.resolve("signing.properties")

    if (signingProperties.isFile && signingProperties.exists()) {
        InputStreamReader(FileInputStream(signingProperties), Charsets.UTF_8).use { reader ->
            properties.load(reader)
        }
    }

    return properties
}
