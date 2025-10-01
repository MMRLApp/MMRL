@file:Suppress("unused", "UnusedReceiverParameter")

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import java.io.File
import java.util.Properties
import java.security.SecureRandom

// #### CONFIG START ####

const val COMPILE_SDK = 36
const val BUILD_TOOLS_VERSION = "$COMPILE_SDK.0.0"
const val MIN_SDK = 26

// ####  CONFIG END  ####

val Project.commitId: String get() = exec("git rev-parse --short HEAD")
val Project.commitCount: Int get() = exec("git rev-list --count HEAD").toInt()

fun Project.exec(command: String): String = providers.exec {
    commandLine(command.split(" "))
}.standardOutput.asText.get().trim()

val Project.releaseKeyStore: File get() = File(extra["keyStore"] as String)
val Project.releaseKeyStorePassword: String get() = extra["keyStorePassword"] as String
val Project.releaseKeyAlias: String get() = extra["keyAlias"] as String
val Project.releaseKeyPassword: String get() = extra["keyPassword"] as String
val Project.hasReleaseKeyStore: Boolean get() {
    signingProperties(rootDir).forEach { key, value ->
        extra[key as String] = value
    }

    return extra.has("keyStore")
}

fun Project.generateRandomName(minLength: Int = 5, maxLength: Int = 12): String {
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    val random = SecureRandom()
    val length = random.nextInt(maxLength - minLength + 1) + minLength
    return (1..length)
        .map { chars[random.nextInt(chars.length)] }
        .joinToString("")
}

fun Project.generateRandomPackageName(
    segments: Int = 3,
    minLength: Int = 5,
    maxLength: Int = 20
): String {
    val letters = "abcdefghijklmnopqrstuvwxyz"
    val lettersAndDigits = "abcdefghijklmnopqrstuvwxyz0123456789"
    val random = SecureRandom()

    return (1..segments).joinToString(".") {
        val length = random.nextInt(maxLength - minLength + 1) + minLength
        val firstChar = letters[random.nextInt(letters.length)] // must start with a letter
        val rest = (1 until length)
            .map { lettersAndDigits[random.nextInt(lettersAndDigits.length)] }
            .joinToString("")
        firstChar + rest
    }
}

private fun signingProperties(rootDir: File): Properties {
    val properties = Properties()
    val signingProperties = rootDir.resolve("signing.properties")
    if (signingProperties.isFile) {
        signingProperties.inputStream().use(properties::load)
    }

    return properties
}