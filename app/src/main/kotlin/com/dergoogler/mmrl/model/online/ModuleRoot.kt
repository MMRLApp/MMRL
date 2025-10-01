package com.dergoogler.mmrl.model.online

import com.squareup.moshi.JsonClass
import io.github.z4kn4fein.semver.constraints.toConstraint
import io.github.z4kn4fein.semver.satisfies
import io.github.z4kn4fein.semver.toVersionOrNull
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
@JsonClass(generateAdapter = true)
data class ModuleRoot(
    val magisk: String? = null,
    val kernelsu: String? = null,
    val apatch: String? = null,
) {
    private fun isNotEmpty() =
        magisk.orEmpty().isNotEmpty() || kernelsu.orEmpty().isNotEmpty() || apatch.orEmpty()
            .isNotEmpty()

    fun isNotSupported(version: String): Boolean {
        val parsedRootProvider = version.replace(Regex("^.*:"), "").lowercase(Locale.ROOT)
        val parsedVersion = version.replace(Regex(":.*$"), "").toVersionOrNull(strict = false)

        if (isNotEmpty()) {
            if (parsedVersion != null) {
                return when (parsedRootProvider) {
                    "magisk" -> {
                        parsedVersion satisfies magisk!!.toConstraint()
                    }

                    "kernelsu" -> {
                        parsedVersion satisfies kernelsu!!.toConstraint()
                    }

                    "apatch" -> {
                        parsedVersion satisfies apatch!!.toConstraint()
                    }

                    else -> true
                }
            } else {
                return true
            }
        } else {
            return true
        }
    }
}

data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {
    companion object {
        fun parse(version: String): Version {
            // Remove any prefix "v" and split by '.'
            val cleanedVersion = version.removePrefix("v")
            val parts = cleanedVersion.split(".")

            // Extract major, minor, and patch (default to 0 if not provided)
            val major = parts.getOrNull(0)?.toInt() ?: 0
            val minor = parts.getOrNull(1)?.toInt() ?: 0
            val patch = parts.getOrNull(2)?.toInt() ?: 0

            return Version(major, minor, patch)
        }
    }

    override fun compareTo(other: Version): Int {
        return when {
            major != other.major -> major - other.major
            minor != other.minor -> minor - other.minor
            else -> patch - other.patch
        }
    }
}

fun isVersionSatisfies(version: String, constraint: String): Boolean {
    val versionParts = constraint.split(" ", limit = 2)
    val operator = versionParts[0]
    val versionToCompare = Version.parse(versionParts[1])

    return when (operator) {
        ">=" -> Version.parse(version) >= versionToCompare
        ">" -> Version.parse(version) > versionToCompare
        "<=" -> Version.parse(version) <= versionToCompare
        "<" -> Version.parse(version) < versionToCompare
        "==" -> Version.parse(version) == versionToCompare
        "!=" -> Version.parse(version) != versionToCompare
        else -> throw IllegalArgumentException("Unknown operator: $operator")
    }
}

fun main() {
    val version = "v1.2.3"
    val constraint = ">= 1.0.0"

    if (isVersionSatisfies(version, constraint)) {
        println("$version satisfies the constraint $constraint")
    } else {
        println("$version does not satisfy the constraint $constraint")
    }
}
