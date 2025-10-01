package com.dergoogler.mmrl.ext

import android.R.attr.text
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.io.path.nameWithoutExtension


/**
 * Finds the first file in the given directory that matches the given glob pattern.
 * @param   prefix
 *          what the file should starts with
 * @param   patterns
 *          what should be searched for. Default is `*.apk`, `*.jar`, `*.dex`
 *
 * @return  a new `Path` object
 */
fun String.findFileGlob(
    prefix: String, vararg patterns: String = arrayOf("*.apk", "*.jar", "*.dex"),
): Path? {
    val dirPath: Path = Paths.get(this)

    Files.newDirectoryStream(dirPath).use { directoryStream ->
        for (path in directoryStream) {
            for (pattern in patterns) {
                val pathMatcher = FileSystems.getDefault().getPathMatcher("glob:$pattern")
                if (pathMatcher.matches(path.fileName) && path.fileName.nameWithoutExtension == prefix) {
                    return path
                }
            }
        }
    }

    return null
}

inline fun <R> String?.ifNotNullOrBlank(block: (String) -> R): R? {
    return if (!this.isNullOrBlank()) block(this) else null
}

@OptIn(ExperimentalContracts::class)
fun CharSequence?.isNotNullOrBlank(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrBlank != null)
    }

    return !this.isNullOrBlank()
}

private fun isUrlEncoded(url: String): Boolean {
    return try {
        val decoded = URLDecoder.decode(url, "UTF-8")
        val reEncoded = URLEncoder.encode(decoded, "UTF-8")
        url == reEncoded
    } catch (e: Exception) {
        false
    }
}

fun String.toDecodedUrl(force: Boolean = false): String = if (force || isUrlEncoded(this)) {
    URLDecoder.decode(this, "UTF-8")
} else {
    this
}

fun String.toEncodedUrl(): String = if (isUrlEncoded(this)) {
    this
} else {
    URLEncoder.encode(this, "UTF-8")
}

val String.repoId
    get(): String {
        val uri = URI(this)
        val cleanedText = (uri.host + uri.path).replace(Regex("[.:/\\-_]"), "")

        val length = cleanedText.length
        val middleStart = (length - 3) / 2
        val middleEnd = middleStart + 3

        val firstThree = cleanedText.take(3)
        val middleThree = cleanedText.substring(middleStart, middleEnd)
        val lastThree = cleanedText.takeLast(3)

        return firstThree + middleThree + lastThree
    }

fun String.isLocalWifiUrl(
    regex: Regex = Regex(
        "^(https?://)?(localhost|127\\.0\\.0\\.1|::1|10(?:\\.\\d{1,3}){3}|172\\.(?:1[6-9]|2\\d|3[01])(?:\\.\\d{1,3}){2}|192\\.168(?:\\.\\d{1,3}){2})(?::([0-9]{1,5}))?$"
    ),
): Boolean {
    return try {
        val uri = URI(this)
        val host = uri.host ?: return false
        val port = uri.port

        host.matches(regex) && (port == -1 || port in 1..65535)
    } catch (e: Exception) {
        false
    }
}

/**
 * Strips all URLs from a string and replaces them with a given replacement string.
 *
 * @param replacement The string to replace URLs with. Defaults to `[LINK]`.
 * @return The string with all URLs replaced.
 */
fun String.stripLinks(replacement: String = "[LINK]"): String {
    val regex = Regex(
        """\b((?:https?://)?(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,}(?:/[^\s]*)?)\b"""
    )

    return this.replace(regex, replacement)
        .replace(Regex("${Regex.escape(replacement)}/+"), replacement)
}